import assert from 'node:assert/strict'
import { spawnSync } from 'node:child_process'
import { chromium } from 'playwright'

const browser = await chromium.launch({ executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe', headless: true })
const page = await browser.newPage({ viewport: { width: 1440, height: 900 } })
const errors = []
page.on('pageerror', error => errors.push(error.message))
const marker = `smoke-${Date.now()}`
let createdId
let venueId
let noticeId

async function login(username, password) {
  await page.goto('http://localhost:5173/activities')
  await page.getByRole('button', { name: '登录 / 注册' }).click()
  await page.locator('.auth-tabs').getByRole('button', { name: '登录' }).click()
  await page.locator('.small-modal input[autocomplete="username"]').fill(username)
  await page.locator('.small-modal input[type="password"]').fill(password)
  await page.locator('.small-modal .primary-button').click()
  await page.getByRole('button', { name: /同学|管理员/ }).first().waitFor()
}

try {
  await page.goto('http://localhost:5173/activities')
  await page.locator('.activity-card').first().waitFor()
  assert.ok(await page.locator('.activity-card').count() >= 3)
  await page.getByRole('button', { name: '羽毛球', exact: true }).first().click()
  await page.locator('.activity-card').first().waitFor()
  assert.ok(await page.locator('.activity-card').count() >= 1)
  for (const label of await page.locator('.activity-card .sport-label').allTextContents()) assert.equal(label, '羽毛球')
  await page.getByRole('button', { name: '登录 / 注册' }).click()
  await page.locator('.auth-tabs').getByRole('button', { name: '注册' }).click()
  await page.locator('.small-modal input[autocomplete="username"]').fill(marker)
  await page.locator('.small-modal input[type="password"]').fill('Test123456')
  await page.locator('.small-modal input:not([type="password"]):not([autocomplete="username"])').fill('浏览器测试')
  await page.locator('.small-modal .primary-button').click()
  await page.getByRole('button', { name: '浏览器测试' }).click()
  await page.locator('.profile-form input').nth(1).fill('浏览器验证')
  await page.getByRole('button', { name: '保存资料' }).click()
  await page.getByRole('button', { name: '浏览器验证' }).waitFor()
  await page.getByRole('button', { name: '退出登录' }).click()
  await login('demo_chen', '123456')
  await page.getByRole('button', { name: '发布活动' }).first().click()
  await page.locator('.modal input[required]').first().fill(marker)
  const start = new Date(Date.now() + 7 * 86400000)
  const end = new Date(start.getTime() + 3600000)
  const inputDate = date => new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16)
  await page.locator('input[type="datetime-local"]').first().fill(inputDate(start))
  await page.locator('input[type="datetime-local"]').last().fill(inputDate(end))
  await page.getByRole('button', { name: '发布活动' }).last().click()
  await page.waitForURL(/\/activities\/\d+$/)
  createdId = Number(page.url().split('/').pop())
  assert.equal(await page.locator('.detail-title-row h1').innerText(), marker)
  await page.getByRole('button', { name: '编辑' }).click()
  await page.locator('.modal input[required]').first().fill(`${marker}-edited`)
  await page.getByRole('button', { name: '保存修改' }).click()
  await page.getByRole('heading', { name: `${marker}-edited` }).waitFor()
  await page.getByRole('link', { name: '我的活动' }).click()
  await page.locator('.mine-item').first().waitFor()
  await page.getByRole('link', { name: '运动场地' }).click()
  await page.locator('.venue-item').first().waitFor()
  assert.ok(await page.locator('.venue-item').count() >= 4)
  await page.getByRole('link', { name: '校园公告' }).click()
  await page.locator('.notice-item').first().waitFor()
  assert.ok(await page.locator('.notice-item').count() >= 1)

  await page.getByRole('button', { name: '陈同学' }).click()
  await page.getByRole('button', { name: '退出登录' }).click()
  await login('demo_lin', '123456')
  await page.getByRole('link', { name: '发现活动' }).click()
  await page.locator('.activity-card').filter({ hasText: '周末操场轻松跑 5 公里' }).click()
  await page.getByRole('button', { name: '加入这场运动' }).click()
  await page.getByRole('button', { name: '取消报名' }).waitFor()
  await page.getByRole('button', { name: '取消报名' }).click()
  await page.getByRole('button', { name: '加入这场运动' }).waitFor()
  await page.locator('.comment-form textarea').fill(marker)
  await page.getByRole('button', { name: '发布评论' }).click()
  await page.locator('.comment').filter({ hasText: marker }).waitFor()
  page.once('dialog', dialog => dialog.accept())
  await page.locator('.comment').filter({ hasText: marker }).getByTitle('删除评论').click()
  await page.locator('.comment').filter({ hasText: marker }).waitFor({ state: 'hidden' })
  assert.equal(await page.locator('.comment').filter({ hasText: marker }).count(), 0)
  await page.getByRole('button', { name: '林同学' }).click()
  await page.getByRole('button', { name: '退出登录' }).click()

  await login('admin', process.env.ADMIN_PASSWORD || '123456')
  await page.getByRole('link', { name: '场地管理' }).click()
  await page.getByRole('button', { name: '新增场地' }).click()
  await page.locator('.modal input[required]').nth(0).fill(marker)
  await page.locator('.modal input[required]').nth(1).fill('测试区域')
  await page.getByRole('button', { name: '保存场地' }).click()
  await page.locator('.venue-item').filter({ hasText: marker }).waitFor()
  const venueRows = await page.evaluate(async name => {
    const result = await fetch('/api/venues').then(response => response.json())
    return result.data.filter(item => item.name === name)
  }, marker)
  venueId = venueRows[0]?.id
  await page.getByRole('link', { name: '公告管理' }).click()
  await page.getByRole('button', { name: '发布公告' }).click()
  await page.locator('.modal input[required]').fill(marker)
  await page.locator('.modal textarea').fill('前端管理员发布验证')
  await page.getByRole('button', { name: '发布公告' }).last().click()
  await page.locator('.notice-item').filter({ hasText: marker }).waitFor()
  const noticeRows = await page.evaluate(async name => {
    const result = await fetch('/api/notices').then(response => response.json())
    return result.data.filter(item => item.title === name)
  }, marker)
  noticeId = noticeRows[0]?.id
  assert.deepEqual(errors, [])
  console.log(JSON.stringify({ ok: true, marker, createdId, venueId, noticeId }))
} catch (error) {
  console.error(JSON.stringify({ ok: false, marker, createdId, venueId, noticeId, error: error.message, pageErrors: errors }))
  process.exitCode = 1
} finally {
  await browser.close()
  const mysql = process.env.MYSQL_EXE || 'F:/lichunru/web/mysql/mysql-5.7.24-winx64/bin/mysql.exe'
  const sql = `DELETE FROM activity WHERE title LIKE '${marker}%'; DELETE FROM sport_venue WHERE name='${marker}'; DELETE FROM sys_notice WHERE title='${marker}'; DELETE FROM lvyin_sport.user WHERE username='${marker}';`
  const cleanup = spawnSync(mysql, ['--default-character-set=utf8mb4', '-ulcr', 'lvyin_sport', '-e', sql], {
    env: { ...process.env, MYSQL_PWD: process.env.DB_PASSWORD || '123456' }, encoding: 'utf8'
  })
  if (cleanup.status !== 0) {
    console.error(`Test data cleanup failed: ${cleanup.error?.message || cleanup.stderr}`)
    process.exitCode = 1
  }
}
