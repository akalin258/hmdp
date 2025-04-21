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

// 更新用户状态（启用/禁用）
export function updateUserStatus(id, status) {
  return request({
    url: `/user/status`,
    method: 'put',
    data: {
      id,
      status
    }
  })
}

// 重置用户密码
export function resetUserPassword(id) {
  return request({
    url: `/user/reset-password/${id}`,
    method: 'put'
  })
} 