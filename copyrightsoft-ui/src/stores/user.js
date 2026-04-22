import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    username: localStorage.getItem('username') || null,
    userId: localStorage.getItem('userId') || null,
    isAuthenticated: !!localStorage.getItem('token')
  }),

  actions: {
    login(token, username, userId) {
      this.token = token
      this.username = username
      this.userId = userId
      this.isAuthenticated = true

      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
      localStorage.setItem('userId', userId)
    },

    logout() {
      this.token = null
      this.username = null
      this.userId = null
      this.isAuthenticated = false

      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userId')
    },

    updateUserInfo(username, userId) {
      this.username = username
      this.userId = userId

      localStorage.setItem('username', username)
      localStorage.setItem('userId', userId)
    }
  },

  getters: {
    getToken: (state) => state.token,
    getUsername: (state) => state.username,
    getUserId: (state) => state.userId,
    getIsAuthenticated: (state) => state.isAuthenticated
  }
})