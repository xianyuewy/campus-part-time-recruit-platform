import request from '../utils/request'

export const pageStudentJobs = (params) => request({ url: '/api/student/jobs', method: 'get', params })
export const getStudentJobDetail = (jobId) => request({ url: `/api/student/jobs/${jobId}`, method: 'get' })
export const applyJob = (data) => request({ url: '/api/student/jobs/apply', method: 'post', data })
export const myApplications = (params) => request({ url: '/api/student/jobs/applications', method: 'get', params })
export const cancelApplication = (appId) => request({ url: `/api/student/jobs/applications/${appId}/cancel`, method: 'post' })
export const reviewCompany = (data) => request({ url: '/api/student/jobs/reviews', method: 'post', data })
export const myGivenReviews = (params) => request({ url: '/api/student/jobs/reviews/given', method: 'get', params })
export const myReceivedReviews = (params) => request({ url: '/api/student/jobs/reviews/received', method: 'get', params })
export const favoriteJob = (jobId) => request({ url: `/api/student/jobs/${jobId}/favorite`, method: 'post' })
export const unfavoriteJob = (jobId) => request({ url: `/api/student/jobs/${jobId}/favorite`, method: 'delete' })
export const myFavorites = () => request({ url: '/api/student/jobs/favorites', method: 'get' })

export const getMyResume = () => request({ url: '/api/student/resume', method: 'get' })
export const saveMyResume = (data) => request({ url: '/api/student/resume', method: 'put', data })
export const uploadResumeAttachment = (formData) =>
    request({ url: '/api/student/resume/attachment', method: 'post', data: formData })

export const getStudentCredit = () => request({ url: '/api/student/credit', method: 'get' })
export const getStudentCreditAdjustLogs = (params) => request({ url: '/api/student/credit/adjust-logs', method: 'get', params })
/** params: current, size, scope=INITIATED|RECEIVED|ALL */
export const getStudentCreditDisputes = (params) => request({ url: '/api/student/credit/disputes', method: 'get', params })
export const submitStudentCreditDispute = (data) => request({ url: '/api/student/credit/disputes', method: 'post', data })
export const submitStudentDisputeSupplement = (id, formData) =>
    request({ url: `/api/student/credit/disputes/${id}/supplement`, method: 'post', data: formData })
export const getStudentDisputeCounterparties = () =>
    request({ url: '/api/student/credit/dispute-counterparties', method: 'get' })
export const getStudentDisputeRelationPicks = (params) =>
    request({ url: '/api/student/credit/dispute-relation-picks', method: 'get', params })
// AI 解析简历（仅回填，不保存）
export function parseResume(data) {
    return request({
        url: '/api/student/resume/parse',
        method: 'post',
        data
    })
}
// 新增1：提交简历解析任务，立即返回任务ID
export function submitParseTask(data) {
    return request({
        url: '/api/student/resume/parse/submit',
        method: 'post',
        data
    })
}

// 新增2：根据任务ID查询解析结果
export function getParseResult(taskId) {
    return request({
        url: '/api/student/resume/parse/result',
        method: 'get',
        params: { taskId }
    })
}