import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8082',  // 直接指向你的后端服务，确保它运行在8082端口
  timeout: 15000, // 请求超时时间
  withCredentials: true // 允许跨域请求携带cookie
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 对于session-based认证，不需要添加token
    // 浏览器会自动发送cookie，保持session
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 根据后端的success字段判断请求是否成功，而不是code
    if (!res.success) {
      ElMessage.error(res.errorMsg || '请求失败')
      
      // 如果需要处理未授权的情况，可以在这里添加额外逻辑
      // 例如：检查错误消息中是否包含"未登录"或"未授权"等关键词
      if (res.errorMsg && (res.errorMsg.includes('登录') || res.errorMsg.includes('授权'))) {
        // 清除登录标记并跳转到登录页
        localStorage.removeItem('isLoggedIn')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.errorMsg || '请求失败'))
    } else {
      return res  // 返回完整响应，包括data字段
    }
  },
  error => {
    console.error('响应错误：', error)
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      ElMessage.error('未登录或登录已过期，请重新登录')
      // 清除登录标记
      localStorage.removeItem('isLoggedIn')
      // 跳转到登录页
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request