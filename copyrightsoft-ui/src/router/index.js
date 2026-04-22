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
    meta: { requiresAuth: true }
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
        meta: { requiresAuth: true }
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
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  if (requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
