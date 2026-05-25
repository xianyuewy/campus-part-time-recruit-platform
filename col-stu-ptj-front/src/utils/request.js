import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
export const API_BASE_URL = 'http://localhost:8080'

const service = axios.create({
    baseURL: API_BASE_URL, // 后端API地址
    timeout: 10000, // 请求超时时间
    withCredentials: true // 允许携带cookie
})

// 刷新token使用独立实例，避免拦截器递归
const refreshClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    withCredentials: true
})

let isRefreshing = false
let waitingQueue = []
let refreshTimer = null

const REFRESH_AHEAD_SECONDS = 90
const MIN_REFRESH_INTERVAL_MS = 15 * 1000

const clearAuthAndGoLogin = () => {
    clearProactiveTimer()
    localStorage.removeItem('accessToken')
    localStorage.removeItem('userInfo')
    if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
    }
}

const isAuthEndpoint = (url = '') => (
    url.includes('/api/auth/login')
    || url.includes('/api/auth/register')
    || url.includes('/api/auth/refresh')
    || url.includes('/api/auth/logout')
)

const parseApiErrorMessage = (error) => {
    const data = error.response?.data
    return (data && (data.message || data.msg)) || error.message || '请求失败'
}

const shouldTryRefresh = (error) => {
    const status = error.response?.status
    const code = error.response?.data?.code
    return status === 401 || code === 401
}

const enqueueRequest = () => new Promise((resolve, reject) => {
    waitingQueue.push({ resolve, reject })
})

const flushQueue = (token, err) => {
    waitingQueue.forEach(({ resolve, reject }) => {
        if (err) reject(err)
        else resolve(token)
    })
    waitingQueue = []
}

const doRefreshToken = async () => {
    const res = await refreshClient.post('/api/auth/refresh')
    const body = res.data
    if (!body || body.code !== 200 || !body.data?.accessToken) {
        throw new Error(body?.message || '刷新令牌失败')
    }
    const { accessToken, ...userInfo } = body.data
    localStorage.setItem('accessToken', accessToken)
    if (userInfo && Object.keys(userInfo).length) {
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
    }
    scheduleProactiveRefresh(accessToken)
    return accessToken
}

const decodeJwtPayload = (token) => {
    if (!token || typeof token !== 'string') return null
    const parts = token.split('.')
    if (parts.length < 2) return null
    try {
        const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
        const padded = base64 + '='.repeat((4 - base64.length % 4) % 4)
        return JSON.parse(atob(padded))
    } catch {
        return null
    }
}

const clearProactiveTimer = () => {
    if (refreshTimer) {
        clearTimeout(refreshTimer)
        refreshTimer = null
    }
}

const tryProactiveRefresh = async () => {
    const token = localStorage.getItem('accessToken')
    if (!token) {
        clearProactiveTimer()
        return
    }
    if (document.visibilityState === 'hidden' || !navigator.onLine) {
        scheduleProactiveRefresh(token)
        return
    }
    try {
        if (!isRefreshing) {
            isRefreshing = true
            const newToken = await doRefreshToken()
            flushQueue(newToken)
        }
    } catch (e) {
        flushQueue(null, e)
        clearAuthAndGoLogin()
    } finally {
        isRefreshing = false
    }
}

const scheduleProactiveRefresh = (token) => {
    clearProactiveTimer()
    const payload = decodeJwtPayload(token)
    const expSec = payload?.exp
    if (!expSec) return
    const nowMs = Date.now()
    const runAtMs = expSec * 1000 - REFRESH_AHEAD_SECONDS * 1000
    const delayMs = Math.max(MIN_REFRESH_INTERVAL_MS, runAtMs - nowMs)
    refreshTimer = setTimeout(tryProactiveRefresh, delayMs)
}

export const initTokenAutoRefresh = () => {
    const token = localStorage.getItem('accessToken')
    if (token) {
        scheduleProactiveRefresh(token)
    }
    document.addEventListener('visibilitychange', () => {
        const t = localStorage.getItem('accessToken')
        if (!t) return
        if (document.visibilityState === 'visible') {
            const payload = decodeJwtPayload(t)
            if (!payload?.exp) return
            const remainMs = payload.exp * 1000 - Date.now()
            if (remainMs <= REFRESH_AHEAD_SECONDS * 1000) {
                void tryProactiveRefresh()
            } else {
                scheduleProactiveRefresh(t)
            }
        }
    })
    window.addEventListener('online', () => {
        const t = localStorage.getItem('accessToken')
        if (t) scheduleProactiveRefresh(t)
    })
}

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 在请求头中添加token
        const token = localStorage.getItem('accessToken')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        if (config.data instanceof FormData) {
            delete config.headers['Content-Type']
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data

        // 后端业务码非200，转成错误对象进入 error 分支
        if (res.code !== 200) {
            const err = new Error(res.message || 'Error')
            err.response = { ...response, data: res }
            err.config = response.config
            return Promise.reject(err)
        }
        return res
    },
    async error => {
        const originalRequest = error.config || {}

        if (
            shouldTryRefresh(error)
            && !originalRequest._retry
            && !isAuthEndpoint(originalRequest.url)
        ) {
            originalRequest._retry = true
            try {
                if (isRefreshing) {
                    const token = await enqueueRequest()
                    originalRequest.headers = originalRequest.headers || {}
                    originalRequest.headers['Authorization'] = 'Bearer ' + token
                    return service(originalRequest)
                }

                isRefreshing = true
                const token = await doRefreshToken()
                flushQueue(token)

                originalRequest.headers = originalRequest.headers || {}
                originalRequest.headers['Authorization'] = 'Bearer ' + token
                return service(originalRequest)
            } catch (refreshError) {
                flushQueue(null, refreshError)
                clearProactiveTimer()
                clearAuthAndGoLogin()
                ElMessage({
                    message: '登录状态已过期，请重新登录',
                    type: 'error',
                    duration: 5 * 1000
                })
                return Promise.reject(refreshError)
            } finally {
                isRefreshing = false
            }
        }

        const msg = parseApiErrorMessage(error)
        console.error('API error:', error.response?.status, msg)
        ElMessage({
            message: msg,
            type: 'error',
            duration: 5 * 1000
        })

        if (shouldTryRefresh(error) && isAuthEndpoint(originalRequest.url)) {
            clearProactiveTimer()
            clearAuthAndGoLogin()
        }
        return Promise.reject(error)
    }
)

/** 投递实时聊天 WebSocket（与 API_BASE_URL 同主机，query 带 token、applicationId） */
export function applicationChatWebSocketUrl(applicationId) {
  const u = new URL(API_BASE_URL)
  const wsProto = u.protocol === 'https:' ? 'wss' : 'ws'
  const token = localStorage.getItem('accessToken') || ''
  const q = new URLSearchParams({
    token,
    applicationId: String(applicationId)
  })
  return `${wsProto}://${u.host}/ws/application-chat?${q.toString()}`
}

export default service