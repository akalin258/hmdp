import request from '../utils/request'

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

// 重置用户密码
export function resetUserPassword(id) {
  return request({
    url: `/user/reset-password/${id}`,
    method: 'put'
  })
} 