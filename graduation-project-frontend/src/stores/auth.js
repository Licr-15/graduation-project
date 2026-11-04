import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api.js'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('lvyin_token') || '')
  const user = ref(JSON.parse(localStorage.getItem('lvyin_user') || 'null'))
  function setSession(login) {
    token.value = login.token
    user.value = login.user
    localStorage.setItem('lvyin_token', login.token)
    localStorage.setItem('lvyin_user', JSON.stringify(login.user))
  }
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('lvyin_token')
    localStorage.removeItem('lvyin_user')
  }
  async function refresh() {
    if (!token.value) return
    try {
      user.value = await api.get('/auth/profile')
      localStorage.setItem('lvyin_user', JSON.stringify(user.value))
    } catch { logout() }
  }
  return { token, user, setSession, logout, refresh }
})
