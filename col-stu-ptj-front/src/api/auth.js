import request from '../utils/request'

// 用户注册
export const register = (data) => {
    return request({
        url: '/api/auth/register',
        method: 'post',
        data
    })
}

// 用户登录
export const login = (data) => {
    return request({
        url: '/api/auth/login',
        method: 'post',
        data
    })
}

// 刷新Token
export const refreshToken = () => {
    return request({
        url: '/api/auth/refresh',
        method: 'post'
    })
}

// 获取当前用户信息
export const getCurrentUser = () => {
    return request({
        url: '/api/auth/me',
        method: 'get'
    })
}

// 用户登出
export const logout = () => {
    return request({
        url: '/api/auth/logout',
        method: 'post'
    })
}

/** 更新邮箱、手机 */
export const updateUserProfile = (data) =>
    request({ url: '/api/user/profile', method: 'put', data })

/** 修改密码 */
export const changeUserPassword = (data) =>
    request({ url: '/api/user/password', method: 'put', data })

/** 上传头像 multipart field: file */
export const uploadUserAvatar = (formData) =>
    request({ url: '/api/user/avatar', method: 'post', data: formData })

/** 学生资质信息 */
export const getStudentQualification = () =>
  request({ url: '/api/user/student/qualification', method: 'get' })

/** 上传学生资质材料 multipart field: file */
export const uploadStudentQualification = (formData) =>
  request({ url: '/api/user/student/qualification', method: 'post', data: formData })

/** 提交学生资质审核 */
export const submitStudentAudit = () =>
  request({ url: '/api/user/student/submit-audit', method: 'post' })

/** 首页欢迎区统计（metricA/B + labelA/B，含义随角色变化） */
export const getHomeSummary = () =>
    request({ url: '/api/user/home-summary', method: 'get' })