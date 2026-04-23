import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    username: localStorage.getItem('username') || null,
    userId: localStorage.getItem('userId') || null,
    role: localStorage.getItem('role') || null,
    isAuthenticated: !!localStorage.getItem('token')
  }),

  actions: {
    login(token, username, userId, role) {
      this.token = token
      this.username = username
      this.userId = userId
      this.role = role
      this.isAuthenticated = true

      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
      localStorage.setItem('userId', userId)
      localStorage.setItem('role', role)
    },

    logout() {
      this.token = null
      this.username = null
      this.userId = null
      this.role = null
      this.isAuthenticated = false

      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
    },

    updateUserInfo(username, userId, role) {
      this.username = username
      this.userId = userId
      this.role = role

      localStorage.setItem('username', username)
      localStorage.setItem('userId', userId)
      if (role) {
        localStorage.setItem('role', role)
      }
    }
  },

  getters: {
    getToken: (state) => state.token,
    getUsername: (state) => state.username,
    getUserId: (state) => state.userId,
    getRole: (state) => state.role,
    getIsAuthenticated: (state) => state.isAuthenticated,
    isAdmin: (state) => state.role === 'ADMIN'
  }
})
