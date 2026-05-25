import request from '../utils/request'

/** 看板统计 */
export const getDashboardStats = () => {
  return request({
    url: '/api/admin/dashboard/stats',
    method: 'get'
  })
}

/** 用户列表（管理） */
export const listUsers = (params) => {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params
  })
}

/** 待审核用户分页 */
export const getPendingUsers = (params) => {
  return request({
    url: '/api/admin/users/pending',
    method: 'get',
    params
  })
}

export const approveUser = (id) => {
  return request({
    url: `/api/admin/users/${id}/approve`,
    method: 'post'
  })
}

export const rejectUser = (id, remark) => {
  return request({
    url: `/api/admin/users/${id}/reject`,
    method: 'post',
    data: { remark }
  })
}

export const disableUser = (id) => {
  return request({
    url: `/api/admin/users/${id}/disable`,
    method: 'post'
  })
}

export const enableUser = (id) => {
  return request({
    url: `/api/admin/users/${id}/enable`,
    method: 'post'
  })
}

/** 岗位列表（全量） */
export const listJobs = (params) => {
  return request({
    url: '/api/admin/jobs',
    method: 'get',
    params
  })
}

/** 待审核岗位分页 */
export const getPendingJobs = (params) => {
  return request({
    url: '/api/admin/jobs/pending',
    method: 'get',
    params
  })
}

export const approveJob = (id) => {
  return request({
    url: `/api/admin/jobs/${id}/approve`,
    method: 'post'
  })
}

export const rejectJob = (id, remark) => {
  return request({
    url: `/api/admin/jobs/${id}/reject`,
    method: 'post',
    data: { remark }
  })
}

export const offlineJob = (id) => {
  return request({
    url: `/api/admin/jobs/${id}/offline`,
    method: 'post'
  })
}

/** 信用档案 */
export const listCreditProfiles = (params) => {
  return request({
    url: '/api/admin/credit/profiles',
    method: 'get',
    params
  })
}

export const adjustCredit = (data) => {
  return request({
    url: '/api/admin/credit/adjust',
    method: 'post',
    data
  })
}

/** 争议工单 */
export const listCreditDisputes = (params) => {
  return request({
    url: '/api/admin/credit/disputes',
    method: 'get',
    params
  })
}

export const resolveCreditDispute = (id, data) => {
  return request({
    url: `/api/admin/credit/disputes/${id}/resolve`,
    method: 'post',
    data
  })
}

/** 系统配置 */
export const listSysConfig = () => {
  return request({
    url: '/api/admin/config',
    method: 'get'
  })
}

export const upsertSysConfig = (data) => {
  return request({
    url: '/api/admin/config',
    method: 'put',
    data
  })
}

/** 上传首页轮播图，返回 data.url 如 /uploads/banners/xxx.jpg */
export const uploadAdminBannerImage = (formData) =>
  request({
    url: '/api/admin/config/banner-image',
    method: 'post',
    data: formData
  })
