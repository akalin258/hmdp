import request from '../utils/request'

// 获取店铺列表
export function getShopList(params) {
  return request({
    url: '/shop/list',
    method: 'get',
    params
  })
}

// 获取店铺详情
export function getShopDetail(id) {
  return request({
    url: `/shop/${id}`,
    method: 'get'
  })
}

// 新增店铺
export function addShop(data) {
  return request({
    url: '/shop',
    method: 'post',
    data
  })
}

// 更新店铺
export function updateShop(data) {
  return request({
    url: '/shop',
    method: 'put',
    data
  })
}

// 删除店铺
export function deleteShop(id) {
  return request({
    url: `/shop/${id}`,
    method: 'delete'
  })
}

// 获取店铺类型列表
export function getShopTypeList() {
  return request({
    url: '/shop-type/list',
    method: 'get'
  })
} 