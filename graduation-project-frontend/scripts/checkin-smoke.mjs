import assert from 'node:assert/strict'
import { spawnSync } from 'node:child_process'
import { chromium } from 'playwright'

const marker = `checkin-smoke-${Date.now()}`
const browser = await chromium.launch({ executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe', headless: true })
const admin = await browser.newPage({ viewport: { width: 1440, height: 900 } })
const student = await browser.newPage({ viewport: { width: 390, height: 844 } })
const errors = []
for (const page of [admin, student]) page.on('pageerror', error => errors.push(error.message))
let userId
let ruleId
let originalMinimum
const mysql = process.env.MYSQL_EXE || 'F:/lichunru/web/mysql/mysql-5.7.24-winx64/bin/mysql.exe'
function sql(query) {
  const result = spawnSync(mysql, ['-ulcr', 'lvyin_sport', '-e', query], { env: { ...process.env, MYSQL_PWD: process.env.DB_PASSWORD || '123456' }, encoding: 'utf8' })
  assert.equal(result.status, 0, result.error?.message || result.stderr)
  return result.stdout
}

async function request(page, method, url, data) {
  return page.evaluate(async ({ method, url, data }) => {
    const response = await fetch(`/api${url}`, { method, headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('lvyin_token') || ''}` }, body: data ? JSON.stringify(data) : undefined })
    return response.json()
  }, { method, url, data })
}
async function login(page, username, password, register = false) {
  await page.goto('http://localhost:5173/activities')
  const result = await request(page, 'POST', `/auth/${register ? 'register' : 'login'}`, { username, password, nickname: marker })
  assert.equal(result.success, true, result.message)
  await page.evaluate(({ token, user }) => {
    sessionStorage.setItem('lvyin_token', token)
    sessionStorage.setItem('lvyin_user', JSON.stringify(user))
  }, result.data)
  return result.data.user.id
}

try {
  await login(admin, 'admin', process.env.ADMIN_PASSWORD || '123456')
  await admin.goto('http://localhost:5173/admin/reward-rules')
  await admin.getByRole('heading', { name: '打卡奖励规则' }).waitFor()
  assert.equal(await admin.getByRole('link', { name: '奖励规则' }).count(), 1)
  const policy = await request(admin, 'GET', '/admin/checkin-policy')
  originalMinimum = policy.data.minDurationMinutes
  await admin.getByLabel('最低运动时长（分钟）').fill('25')
  await admin.getByRole('button', { name: '保存条件' }).click()
  await admin.getByText('有效打卡条件已保存').waitFor()
  await admin.getByLabel('规则名称').fill(marker)
  await admin.getByLabel('规则类型').selectOption('DAILY')
  assert.equal(await admin.getByLabel('达成目标（次）').inputValue(), '1')
  await admin.getByLabel('奖励积分').fill('7')
  await admin.getByRole('button', { name: '保存规则' }).click()
  await admin.locator('.xp-item').filter({ hasText: marker }).waitFor()
  const rules = await request(admin, 'GET', '/admin/reward-rules')
  ruleId = rules.data.find(rule => rule.name === marker)?.id
  assert.ok(ruleId)
  for (const [type, points] of [['STREAK', 5], ['TOTAL', 3]]) {
    const result = await request(admin, 'POST', '/admin/reward-rules', { name: `${marker}-${type}`, ruleType: type, targetValue: 2, rewardPoints: points, badgeName: '' })
    assert.equal(result.success, true, result.message)
  }

  userId = await login(student, marker, 'Test123456', true)
  sql(`INSERT INTO sport_checkin(user_id,sport_type,checkin_date,duration_minutes,distance_km) VALUES ${[1,2,3,4].map(days => `(${userId},'羽毛球',DATE_SUB(CURDATE(),INTERVAL ${days} DAY),30,0)`).join(',')}; INSERT INTO user_reward(user_id,rule_id,source_date,award_key,reward_points) VALUES(${userId},${ruleId},DATE_SUB(CURDATE(),INTERVAL 1 DAY),DATE_FORMAT(DATE_SUB(CURDATE(),INTERVAL 1 DAY),'%Y-%m-%d'),7); INSERT INTO point_ledger(user_id,points,reason,source_type,source_id) SELECT ${userId},7,'${marker}奖励','SPORT_REWARD',id FROM user_reward WHERE user_id=${userId} AND rule_id=${ruleId};`)
  await student.goto('http://localhost:5173/checkins')
  await student.getByRole('heading', { name: '每日运动打卡' }).waitFor()
  await student.getByText('每次运动至少 25 分钟', { exact: false }).waitFor()
  assert.equal(await student.getByLabel('运动距离（公里，可选）').count(), 0)
  assert.match(await student.locator('.xp-goal-list').innerText(), /连续打卡 4\/7 天/)
  const tooShort = await request(student, 'POST', '/checkins', { sportType: '羽毛球', durationMinutes: 24, distanceKm: 5 })
  assert.equal(tooShort.success, false)
  assert.match(tooShort.message, /至少 25 分钟/)
  await student.getByLabel('运动类型').fill('羽毛球')
  await student.getByLabel('运动时长（分钟）').fill('35')
  await student.getByRole('button', { name: '今日打卡' }).click()
  await student.getByText('今天已经完成运动打卡').waitFor()
  assert.match(await student.locator('main').innerText(), /累计 5 次/)
  assert.match(await student.locator('main').innerText(), new RegExp(marker))
  const duplicate = await request(student, 'POST', '/checkins', { sportType: '羽毛球', durationMinutes: 35, distanceKm: 0 })
  assert.equal(duplicate.success, false)
  assert.match(duplicate.message, /每天只能打卡一次/)
  const checkins = await request(student, 'GET', '/checkins/mine')
  assert.equal(checkins.data.total, 5)
  assert.equal(checkins.data.streak, 5)
  assert.equal(checkins.data.records[0].distanceKm, 0)
  assert.ok(checkins.data.goals.some(goal => goal.ruleType === 'STREAK' && goal.unit === '天' && goal.progress === 5 && goal.claimed))
  assert.ok(checkins.data.goals.some(goal => goal.ruleType === 'TOTAL' && goal.unit === '次' && goal.progress === 5 && goal.claimed))
  assert.ok(checkins.data.goals.some(goal => goal.ruleType === 'STREAK' && goal.targetValue === 3 && goal.claimed))
  assert.ok(checkins.data.goals.some(goal => goal.ruleType === 'STREAK' && goal.targetValue === 7 && goal.progress === 5 && !goal.claimed))
  assert.ok(checkins.data.goals.some(goal => goal.ruleType === 'TOTAL' && goal.targetValue === 5 && goal.claimed))
  assert.ok(checkins.data.goals.some(goal => goal.ruleType === 'TOTAL' && goal.targetValue === 15 && goal.progress === 5 && !goal.claimed))
  assert.equal(checkins.data.rewards.filter(item => item.name === marker).length, 2)
  assert.ok(checkins.data.rewards.some(item => item.name === `${marker}-STREAK`))
  assert.ok(checkins.data.rewards.some(item => item.name === `${marker}-TOTAL`))
  const points = await request(student, 'GET', '/points/mine')
  assert.equal(points.data.total, points.data.records.reduce((sum, item) => sum + item.points, 0))
  assert.equal(points.data.records.filter(item => item.reason === `${marker}奖励`).length, 2)
  assert.equal(points.data.records.filter(item => item.reason.startsWith(marker)).reduce((sum, item) => sum + item.points, 0), 22)
  await student.goto('http://localhost:5173/points')
  await student.getByText(`${marker}奖励`).first().waitFor()
  assert.equal(await student.evaluate(() => document.documentElement.scrollWidth > innerWidth), false)

  await admin.goto('http://localhost:5173/admin/reward-rules')
  await admin.locator('.xp-item').filter({ has: admin.getByText(marker, { exact: true }) }).getByTitle('停用规则').click()
  await admin.getByText('规则已停用').waitFor()
  const disabled = await request(admin, 'GET', '/admin/reward-rules')
  assert.equal(disabled.data.find(rule => rule.id === ruleId)?.status, 0)
  await admin.locator('.xp-item').filter({ has: admin.getByText(marker, { exact: true }) }).getByRole('button', { name: '启用' }).click()
  await admin.getByText('规则已启用').waitFor()
  const enabled = await request(admin, 'GET', '/admin/reward-rules')
  assert.equal(enabled.data.find(rule => rule.id === ruleId)?.status, 1)
  assert.deepEqual(errors, [])
  console.log(JSON.stringify({ ok: true, userId, ruleId, checkins: 5, streak: 5, testRulePoints: 22, totalPoints: points.data.total, adminRuleToggle: true, browserErrors: errors }))
} catch (error) {
  console.error(JSON.stringify({ ok: false, userId, ruleId, error: error.message, browserErrors: errors }))
  process.exitCode = 1
} finally {
  await browser.close()
  if (originalMinimum !== undefined) sql(`UPDATE sport_checkin_policy SET min_duration_minutes=${originalMinimum} WHERE id=1`)
  const cleanup = spawnSync(mysql, ['-ulcr', 'lvyin_sport', '-e', `DELETE FROM point_ledger WHERE user_id IN (SELECT id FROM user WHERE username='${marker}'); DELETE FROM user_reward WHERE user_id IN (SELECT id FROM user WHERE username='${marker}'); DELETE FROM sport_checkin WHERE user_id IN (SELECT id FROM user WHERE username='${marker}'); DELETE FROM user WHERE username='${marker}'; DELETE FROM reward_rule WHERE name LIKE '${marker}%';`], { env: { ...process.env, MYSQL_PWD: process.env.DB_PASSWORD || '123456' }, encoding: 'utf8' })
  if (cleanup.status !== 0) {
    console.error(`Test data cleanup failed: ${cleanup.error?.message || cleanup.stderr}`)
    process.exitCode = 1
  }
}
