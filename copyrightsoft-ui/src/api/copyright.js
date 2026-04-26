import request from './request'

export function applyCopyright(formData) {
  return request({
    url: '/copyright/apply',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

export function submitApplication(formData) {
  return request({
    url: '/copyright/applications',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

export function getApplicationStatus(applicationNo) {
  return request({
    url: `/copyright/applications/${applicationNo}`,
    method: 'get'
  })
}

export function queryByHash(fileHash) {
  return request({
    url: `/copyright/query/hash/${fileHash}`,
    method: 'get'
  })
}

export function queryByApplicationNo(applicationNo) {
  return request({
    url: `/copyright/query/application/${applicationNo}`,
    method: 'get'
  })
}

export function getMyRecords(params) {
  return request({
    url: '/copyright/my-records',
    method: 'get',
    params
  })
}

export function getEnterpriseRecords(params) {
  return request({
    url: '/copyright/enterprise-records',
    method: 'get',
    params
  })
}
