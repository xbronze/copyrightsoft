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

export function queryByHash(fileHash) {
  return request({
    url: `/copyright/query/hash/${fileHash}`,
    method: 'get'
  })
}

export function queryById(id) {
  return request({
    url: `/copyright/query/id/${id}`,
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
