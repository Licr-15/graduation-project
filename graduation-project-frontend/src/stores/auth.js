import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api.js'

export const useAuthStore = defineStore('auth', () => {
  localStorage.removeItem('lvyin_token')
  localStorage.removeItem('lvyin_user')
  const token = ref(sessionStorage.getItem('lvyin_token') || '')
  const user = ref(JSON.parse(sessionStorage.getItem('lvyin_user') || 'null'))
  function setSession(login) {
    token.value = login.token
    user.value = login.user
    sessionStorage.setItem('lvyin_token', login.token)
    sessionStorage.setItem('lvyin_user', JSON.stringify(login.user))
  }
  function logout() {
    token.value = ''
    user.value = null
    sessionStorage.removeItem('lvyin_token')
    sessionStorage.removeItem('lvyin_user')
  }
  async function refresh() {
    if (!token.value) return
    try {
      user.value = await api.get('/auth/profile')
      sessionStorage.setItem('lvyin_user', JSON.stringify(user.value))
    } catch { logout() }
  }
  return { token, user, setSession, logout, refresh }
})
