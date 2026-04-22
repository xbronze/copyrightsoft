import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000
})

request.interceptors.request.use(
  config => {
    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)

    // 如果是401未授权错误，跳转到登录页
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userId')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    }

    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
