import request from './request'

// 审核 API：审核列表、详情与审核动作提交。
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
