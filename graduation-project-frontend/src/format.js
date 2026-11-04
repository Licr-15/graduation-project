export const sports = ['全部', '羽毛球', '篮球', '足球', '跑步', '乒乓球', '其他']
export const sportImage = type => ({
  羽毛球: '/sports/badminton.jpg', 篮球: '/sports/basketball.jpg',
  足球: '/sports/football.jpg', 跑步: '/sports/running.jpg', 乒乓球: '/sports/table-tennis.jpg'
}[type] || '/sports/running.jpg')
export const dateTime = value => value ? new Date(value).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false }) : ''
export const localInput = value => value ? value.slice(0, 16) : ''
export const isoLocal = value => value?.length === 16 ? `${value}:00` : value
export const isPast = value => Boolean(value && new Date(value).getTime() < Date.now())
