import request from './request'

// 认证 API：登录注册、个人资料与密码维护。
export function login(credentials) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: credentials
  })
}

export function register(userData) {
  return request({
    url: '/auth/register',
    method: 'post',
    data: userData
  })
}

export function registerEnterprise(userData) {
  return request({
    url: '/auth/register/enterprise',
    method: 'post',
    data: userData
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

export function updateProfile(profileData) {
  return request({
    url: '/auth/profile',
    method: 'put',
    data: profileData
  })
}

export function changePassword(passwordData) {
  return request({
    url: '/auth/password',
    method: 'put',
    data: passwordData
  })
}
