import request from './request'

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
