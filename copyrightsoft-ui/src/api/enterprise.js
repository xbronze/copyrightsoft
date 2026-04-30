import request from './request'

// 企业成员 API：成员查询、增改、状态与法务可见范围管理。
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
