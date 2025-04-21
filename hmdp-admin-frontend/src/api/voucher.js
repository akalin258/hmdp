import request from '../utils/request'

// 获取优惠券列表
export function getVoucherList(params) {
  return request({
    url: '/voucher/list',
    method: 'get',
    params
  })
}

// 获取优惠券详情
export function getVoucherDetail(id) {
  return request({
    url: `/voucher/${id}`,
    method: 'get'
  })
}

// 新增普通优惠券
export function addVoucher(data) {
  return request({
    url: '/voucher',
    method: 'post',
    data
  })
}

// 新增秒杀优惠券
export function addSeckillVoucher(data) {
  return request({
    url: '/voucher/seckill',
    method: 'post',
    data
  })
}

// 修改优惠券
export function updateVoucher(data) {
  return request({
    url: '/voucher',
    method: 'put',
    data
  })
}

// 删除优惠券
export function deleteVoucher(id) {
  return request({
    url: `/voucher/${id}`,
    method: 'delete'
  })
} 