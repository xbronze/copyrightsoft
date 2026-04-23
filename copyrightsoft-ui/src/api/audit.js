import request from './request'

export function getAuditRecords(params) {
  return request({
    url: '/audit/records',
    method: 'get',
    params
  })
}

export function getAuditDetail(id) {
  return request({
    url: `/audit/records/${id}`,
    method: 'get'
  })
}

export function submitAuditAction(id, data) {
  return request({
    url: `/audit/records/${id}/action`,
    method: 'post',
    data
  })
}
