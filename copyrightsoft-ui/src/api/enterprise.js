import request from './request'

export function getEnterpriseMembers(params) {
  return request({
    url: '/enterprise/members',
    method: 'get',
    params
  })
}

export function createEnterpriseMember(data) {
  return request({
    url: '/enterprise/members',
    method: 'post',
    data
  })
}

export function updateEnterpriseMember(id, data) {
  return request({
    url: `/enterprise/members/${id}`,
    method: 'put',
    data
  })
}

export function updateEnterpriseMemberStatus(id, status) {
  return request({
    url: `/enterprise/members/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function updateEnterpriseLegalScope(id, enterpriseLegalScope) {
  return request({
    url: `/enterprise/members/${id}/legal-scope`,
    method: 'put',
    data: { enterpriseLegalScope }
  })
}
