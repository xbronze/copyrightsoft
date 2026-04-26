import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    username: localStorage.getItem('username') || null,
    userId: localStorage.getItem('userId') || null,
    role: localStorage.getItem('role') || null,
    accountType: localStorage.getItem('accountType') || null,
    enterpriseId: localStorage.getItem('enterpriseId') || null,
    enterpriseRole: localStorage.getItem('enterpriseRole') || null,
    enterpriseLegalScope: localStorage.getItem('enterpriseLegalScope') || null,
    isAuthenticated: !!localStorage.getItem('token')
  }),

  actions: {
    login(token, username, userId, role, accountType, enterpriseId, enterpriseRole, enterpriseLegalScope) {
      this.token = token
      this.username = username
      this.userId = userId
      this.role = role
      this.accountType = accountType || null
      this.enterpriseId = enterpriseId || null
      this.enterpriseRole = enterpriseRole || null
      this.enterpriseLegalScope = enterpriseLegalScope || null
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
      if (enterpriseRole) {
        localStorage.setItem('enterpriseRole', enterpriseRole)
      } else {
        localStorage.removeItem('enterpriseRole')
      }
      if (enterpriseLegalScope) {
        localStorage.setItem('enterpriseLegalScope', enterpriseLegalScope)
      } else {
        localStorage.removeItem('enterpriseLegalScope')
      }
    },

    logout() {
      this.token = null
      this.username = null
      this.userId = null
      this.role = null
      this.accountType = null
      this.enterpriseId = null
      this.enterpriseRole = null
      this.enterpriseLegalScope = null
      this.isAuthenticated = false

      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
      localStorage.removeItem('accountType')
      localStorage.removeItem('enterpriseId')
      localStorage.removeItem('enterpriseRole')
      localStorage.removeItem('enterpriseLegalScope')
    },

    updateUserInfo(username, userId, role, accountType, enterpriseId, enterpriseRole, enterpriseLegalScope) {
      this.username = username
      this.userId = userId
      this.role = role
      this.accountType = accountType || this.accountType
      this.enterpriseId = enterpriseId || this.enterpriseId
      this.enterpriseRole = enterpriseRole || this.enterpriseRole
      this.enterpriseLegalScope = enterpriseLegalScope || this.enterpriseLegalScope

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
      if (this.enterpriseRole) {
        localStorage.setItem('enterpriseRole', this.enterpriseRole)
      }
      if (this.enterpriseLegalScope) {
        localStorage.setItem('enterpriseLegalScope', this.enterpriseLegalScope)
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
    getEnterpriseRole: (state) => state.enterpriseRole,
    getEnterpriseLegalScope: (state) => state.enterpriseLegalScope,
    getIsAuthenticated: (state) => state.isAuthenticated,
    isAdmin: (state) => state.role === 'ADMIN',
    isDeveloper: (state) => ['INDIVIDUAL_DEVELOPER', 'ENTERPRISE_DEVELOPER', 'USER'].includes(state.role),
    isEnterpriseOwner: (state) => state.enterpriseRole === 'OWNER',
    isEnterpriseLegal: (state) => state.enterpriseRole === 'LEGAL',
    isEnterpriseLegalAllScope: (state) => state.enterpriseRole === 'LEGAL' && state.enterpriseLegalScope === 'ALL'
  }
})
