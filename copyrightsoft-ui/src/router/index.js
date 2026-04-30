import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

/**
 * 前端路由表与统一鉴权入口。
 * - meta.requiresAuth: 需要登录
 * - meta.requiresAdmin / requiresAuditor: 平台角色校验
 * - meta.developerOnly / enterpriseRoles: 主体角色校验
 */
const routes = [
  {
    path: '/',
    name: 'home',
    component: Home,
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/apply',
    name: 'apply',
    component: () => import('../views/ApplyCopyright.vue'),
    meta: { requiresAuth: true, developerOnly: true }
  },
  {
    path: '/query-hash',
    name: 'query-hash',
    component: () => import('../views/QueryByHash.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/copyright/:applicationNo',
    name: 'copyright-detail',
    component: () => import('../views/CopyrightDetail.vue'),
    meta: { requiresAuth: false }
  },
  // 个人中心路由
  {
    path: '/profile',
    name: 'profile',
    redirect: '/profile/records',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'info',
        name: 'profile-info',
        component: () => import('../views/profile/ProfileInfo.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'records',
        name: 'profile-records',
        component: () => import('../views/profile/MyRecords.vue'),
        meta: { requiresAuth: true, developerOnly: true }
      },
      {
        path: 'enterprise-records',
        name: 'profile-enterprise-records',
        component: () => import('../views/profile/EnterpriseRecords.vue'),
        meta: { requiresAuth: true, enterpriseRoles: ['OWNER', 'LEGAL'] }
      },
      {
        path: 'members',
        name: 'profile-enterprise-members',
        component: () => import('../views/profile/EnterpriseMembers.vue'),
        meta: { requiresAuth: true, enterpriseRoles: ['OWNER'] }
      }
    ]
  },
  // 管理员路由
  {
    path: '/admin',
    name: 'admin',
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('../views/admin/AdminDashboard.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      }
    ]
  },
  {
    path: '/audit',
    name: 'audit',
    redirect: '/audit/review',
    meta: { requiresAuth: true, requiresAuditor: true },
    children: [
      {
        path: 'review',
        name: 'audit-review',
        component: () => import('../views/audit/AuditReview.vue'),
        meta: { requiresAuth: true, requiresAuditor: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 统一路由守卫：按“登录 -> 平台角色 -> 主体角色 -> 法务范围”顺序拦截，避免条件冲突。
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const rawRole = localStorage.getItem('role')
  const role = rawRole === 'USER' ? 'INDIVIDUAL_DEVELOPER' : rawRole
  const enterpriseRole = localStorage.getItem('enterpriseRole')
  const enterpriseLegalScope = localStorage.getItem('enterpriseLegalScope')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  const requiresAuditor = to.matched.some(record => record.meta.requiresAuditor)
  const developerOnly = to.matched.some(record => record.meta.developerOnly)
  const enterpriseRolesMeta = to.matched.find(record => Array.isArray(record.meta.enterpriseRoles))?.meta.enterpriseRoles

  if (requiresAuth && !token) {
    next('/login')
  } else if (requiresAdmin && role !== 'ADMIN') {
    next('/')
  } else if (requiresAuditor && !['AUDITOR', 'ADMIN'].includes(role)) {
    next('/')
  } else if (developerOnly && !['INDIVIDUAL_DEVELOPER', 'ENTERPRISE_DEVELOPER'].includes(role)) {
    next('/')
  } else if (enterpriseRolesMeta && !enterpriseRolesMeta.includes(enterpriseRole)) {
    next('/')
  } else if (enterpriseRole === 'LEGAL'
    && to.path === '/profile/enterprise-records'
    && enterpriseLegalScope !== 'ALL') {
    // 法务仅在 ALL 范围下可看企业全量记录，否则回退到个人记录页。
    next('/profile/records')
  } else {
    next()
  }
})

export default router
