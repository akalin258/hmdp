import request from '../utils/request'

// 获取探店笔记列表
export function getBlogList(params) {
  return request({
    url: '/blog/list',
    method: 'get',
    params
  })
}

// 获取探店笔记详情
export function getBlogDetail(id) {
  return request({
    url: `/blog/${id}`,
    method: 'get'
  })
}

// 删除探店笔记
export function deleteBlog(id) {
  return request({
    url: `/blog/${id}`,
    method: 'delete'
  })
} 