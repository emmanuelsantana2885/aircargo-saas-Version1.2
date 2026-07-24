import axios from 'axios'
import { useToastStore } from '../stores/toast'
import { handleApiError } from '../utils/error'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

let isRefreshing = false
let failedQueue = []

function processQueue(error, token) {
  failedQueue.forEach(p => {
    if (error) p.reject(error)
    else p.resolve(token)
  })
  failedQueue = []
}

api.interceptors.request.use(config => {
  const stored = localStorage.getItem('aircargo_auth')
  if (stored) {
    try {
      const { token } = JSON.parse(stored)
      if (token) config.headers.Authorization = `Bearer ${token}`
    } catch {}
  }
  return config
})

api.interceptors.response.use(
  res => res,
  async err => {
    const originalRequest = err.config
    if (err.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }
      originalRequest._retry = true
      isRefreshing = true

      try {
        const stored = localStorage.getItem('aircargo_auth')
        if (stored) {
          const { refreshToken } = JSON.parse(stored)
          if (refreshToken) {
            const res = await axios.post('/api/auth/refresh', { refreshToken })
            const newToken = res.data.accessToken || res.data.token
            const newRefresh = res.data.refreshToken || refreshToken

            const authData = JSON.parse(stored)
            authData.token = newToken
            authData.refreshToken = newRefresh
            localStorage.setItem('aircargo_auth', JSON.stringify(authData))

            processQueue(null, newToken)
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return api(originalRequest)
          }
        }
      } catch (refreshErr) {
        processQueue(refreshErr, null)
        localStorage.removeItem('aircargo_auth')
        window.location.href = '/login'
        return Promise.reject(refreshErr)
      } finally {
        isRefreshing = false
      }
    }
    if (err.response?.status === 403) {
      const url = err.config?.url || ''
      console.warn('[API 403] Sin permiso para:', err.config?.method?.toUpperCase(), url)
      return Promise.reject(err)
    }
    handleApiError(err, useToastStore())
    return Promise.reject(err)
  }
)

export default api
