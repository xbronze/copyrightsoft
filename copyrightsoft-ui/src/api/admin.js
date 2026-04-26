import request from './request'

export function getUsers(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function createUser(data) {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/admin/users/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function resetPassword(id) {
  return request({
    url: `/admin/users/${id}/reset-password`,
    method: 'put'
  })
}

export function updateUserRole(id, role, enterpriseId) {
  return request({
    url: `/admin/users/${id}/role`,
    method: 'put',
    params: { role, enterpriseId }
  })
}

export function getEnterpriseOptions(keyword) {
  return request({
    url: '/admin/enterprises',
    method: 'get',
    params: { keyword }
  })
}

export function getAllCopyrights(params) {
  return request({
    url: '/admin/copyrights',
    method: 'get',
    params
  })
}

export function getStatistics() {
  return request({
    url: '/admin/statistics',
    method: 'get'
  })
}