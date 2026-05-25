import { defineStore } from 'pinia'
import { login, logout, getCurrentUser } from '../api/auth'

export const useUserStore = defineStore('user', {
    state: () => ({
        userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
        token: localStorage.getItem('accessToken') || ''
    }),

    getters: {
        isLoggedIn: (state) => !!state.token,
        username: (state) => state.userInfo?.username || '',
        role: (state) => state.userInfo?.role || '',
        authStatus: (state) => state.userInfo?.authStatus || '',
        /** 后端 role 一般为字符串枚举名，如 ADMIN */
        isAdmin: (state) => {
            const r = state.userInfo?.role
            if (!r) return false
            if (typeof r === 'string') return r === 'ADMIN'
            if (typeof r === 'object') return r.name === 'ADMIN' || r.code === 'ADMIN'
            return false
        },
        isStudent: (state) => {
            const r = state.userInfo?.role
            if (!r) return false
            if (typeof r === 'string') return r === 'STUDENT'
            if (typeof r === 'object') return r.name === 'STUDENT' || r.code === 'STUDENT'
            return false
        },
        isCompany: (state) => {
            const r = state.userInfo?.role
            if (!r) return false
            if (typeof r === 'string') return r === 'COMPANY'
            if (typeof r === 'object') return r.name === 'COMPANY' || r.code === 'COMPANY'
            return false
        }
    },

    actions: {
        // 用户登录
        async userLogin(loginData) {
            try {
                const response = await login(loginData)
                if (response.success && response.data) {
                    const { accessToken, ...userInfo } = response.data

                    // 保存token和用户信息
                    this.token = accessToken
                    this.userInfo = userInfo

                    localStorage.setItem('accessToken', accessToken)
                    localStorage.setItem('userInfo', JSON.stringify(userInfo))

                    return Promise.resolve(response)
                }
            } catch (error) {
                return Promise.reject(error)
            }
        },

        // 获取用户信息
        async getUserInfo() {
            try {
                const response = await getCurrentUser()
                if (response.success && response.data) {
                    this.userInfo = response.data
                    localStorage.setItem('userInfo', JSON.stringify(response.data))
                }
                return response
            } catch (error) {
                return Promise.reject(error)
            }
        },

        // 用户登出
        async userLogout() {
            try {
                await logout()
                this.resetToken()
                return Promise.resolve()
            } catch (error) {
                return Promise.reject(error)
            }
        },

        // 重置token
        resetToken() {
            this.token = ''
            this.userInfo = null
            localStorage.removeItem('accessToken')
            localStorage.removeItem('userInfo')
        }
    }
})