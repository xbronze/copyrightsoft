import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    username: localStorage.getItem('username') || null,
    userId: localStorage.getItem('userId') || null,
    role: localStorage.getItem('role') || null,
    accountType: localStorage.getItem('accountType') || null,
    enterpriseId: localStorage.getItem('enterpriseId') || null,
    isAuthenticated: !!localStorage.getItem('token')
  }),

  actions: {
    login(token, username, userId, role, accountType, enterpriseId) {
      this.token = token
      this.username = username
      this.userId = userId
      this.role = role
       this.accountType = accountType || null
      this.enterpriseId = enterpriseId || null
      this.isAuthenticated = true

      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
      localStorage.setItem('userId', userId)
      localStorage.setItem('role', role)
      if (accountType) {
        localStorage.setItem('accountType', accountType)
      } else {
        localStorage.removeItem('accountType')
      }
      if (enterpriseId) {
        localStorage.setItem('enterpriseId', enterpriseId)
      } else {
        localStorage.removeItem('enterpriseId')
      }
    },

    logout() {
      this.token = null
      this.username = null
      this.userId = null
      this.role = null
      this.accountType = null
      this.enterpriseId = null
      this.isAuthenticated = false

      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
      localStorage.removeItem('accountType')
      localStorage.removeItem('enterpriseId')
    },

    updateUserInfo(username, userId, role, accountType, enterpriseId) {
      this.username = username
      this.userId = userId
      this.role = role
      this.accountType = accountType || this.accountType
      this.enterpriseId = enterpriseId || this.enterpriseId

      localStorage.setItem('username', username)
      localStorage.setItem('userId', userId)
      if (role) {
        localStorage.setItem('role', role)
      }
      if (this.accountType) {
        localStorage.setItem('accountType', this.accountType)
      }
      if (this.enterpriseId) {
        localStorage.setItem('enterpriseId', this.enterpriseId)
      }
    }
  },

  getters: {
    getToken: (state) => state.token,
    getUsername: (state) => state.username,
    getUserId: (state) => state.userId,
    getRole: (state) => state.role,
    getAccountType: (state) => state.accountType,
    getEnterpriseId: (state) => state.enterpriseId,
    getIsAuthenticated: (state) => state.isAuthenticated,
    isAdmin: (state) => state.role === 'ADMIN',
    isDeveloper: (state) => ['INDIVIDUAL_DEVELOPER', 'ENTERPRISE_DEVELOPER', 'USER'].includes(state.role)
  }
})
