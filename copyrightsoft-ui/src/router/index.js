import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: Home,
    meta: { requiresAuth: true }
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
    path: '/query-id',
    name: 'query-id',
    component: () => import('../views/QueryById.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/query-id/:id',
    name: 'query-id-detail',
    component: () => import('../views/QueryById.vue'),
    meta: { requiresAuth: false }
  },
  // 个人中心路由
  {
    path: '/profile',
    name: 'profile',
    redirect: '/profile/info',
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

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const rawRole = localStorage.getItem('role')
  const role = rawRole === 'USER' ? 'INDIVIDUAL_DEVELOPER' : rawRole
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  const requiresAuditor = to.matched.some(record => record.meta.requiresAuditor)
  const developerOnly = to.matched.some(record => record.meta.developerOnly)

  if (requiresAuth && !token) {
    next('/login')
  } else if (requiresAdmin && role !== 'ADMIN') {
    next('/')
  } else if (requiresAuditor && !['AUDITOR', 'ADMIN'].includes(role)) {
    next('/')
  } else if (developerOnly && !['INDIVIDUAL_DEVELOPER', 'ENTERPRISE_DEVELOPER'].includes(role)) {
    next('/')
  } else {
    next()
  }
})

export default router
