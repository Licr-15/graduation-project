import axios from 'axios'

const client = axios.create({ baseURL: '/api', timeout: 12000 })
client.interceptors.request.use(config => {
  const token = localStorage.getItem('lvyin_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
client.interceptors.response.use(response => {
  if (!response.data?.success) throw new Error(response.data?.message || '操作失败')
  return response.data.data
}, error => {
  if (error.response?.status === 401) {
    localStorage.removeItem('lvyin_token')
    localStorage.removeItem('lvyin_user')
    window.dispatchEvent(new Event('lvyin-unauthorized'))
  }
  throw new Error(error.response?.data?.message || (error.code === 'ERR_NETWORK' ? '无法连接后端，请先启动 Spring Boot' : error.message))
})

export const api = {
  get: (url, params) => client.get(url, { params }),
  post: (url, data) => client.post(url, data),
  put: (url, data) => client.put(url, data),
  delete: url => client.delete(url)
}
