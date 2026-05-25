import request from '../utils/request'

export const getCompanyProfile = () => request({ url: '/api/company/profile', method: 'get' })
export const upsertCompanyProfile = (data) => request({ url: '/api/company/profile', method: 'put', data })
export const submitCompanyAudit = () => request({ url: '/api/company/profile/submit-audit', method: 'post' })
export const publishCompanyJob = (data) => request({ url: '/api/company/jobs', method: 'post', data })
export const myCompanyJobs = (params) => request({ url: '/api/company/jobs', method: 'get', params })
export const updateCompanyJob = (jobId, data) => request({ url: `/api/company/jobs/${jobId}`, method: 'put', data })
export const offlineCompanyJob = (jobId) => request({ url: `/api/company/jobs/${jobId}/offline`, method: 'post' })
export const jobApplicants = (jobId, params) => request({ url: `/api/company/jobs/${jobId}/applications`, method: 'get', params })
export const companyApplicationsInbox = (params) => request({ url: '/api/company/applications', method: 'get', params })
export const getApplicantResume = (appId) =>
  request({ url: `/api/company/applications/${appId}/student-resume`, method: 'get' })
export const updateApplicantStatus = (appId, data) => request({ url: `/api/company/applications/${appId}/status`, method: 'post', data })
export const scheduleApplicantInterview = (appId, data) =>
  request({ url: `/api/company/applications/${appId}/interview-schedule`, method: 'post', data })
export const reviewStudent = (data) => request({ url: '/api/company/reviews', method: 'post', data })
export const companyGivenReviews = (params) => request({ url: '/api/company/reviews/given', method: 'get', params })
export const companyReceivedReviews = (params) => request({ url: '/api/company/reviews/received', method: 'get', params })

export const getCompanyCredit = () => request({ url: '/api/company/credit', method: 'get' })
export const getCompanyCreditAdjustLogs = (params) => request({ url: '/api/company/credit/adjust-logs', method: 'get', params })
/** params: current, size, scope=INITIATED|RECEIVED|ALL */
export const getCompanyCreditDisputes = (params) => request({ url: '/api/company/credit/disputes', method: 'get', params })
export const submitCompanyCreditDispute = (data) => request({ url: '/api/company/credit/disputes', method: 'post', data })
export const submitCompanyDisputeSupplement = (id, formData) =>
    request({ url: `/api/company/credit/disputes/${id}/supplement`, method: 'post', data: formData })
export const getCompanyDisputeCounterparties = () =>
  request({ url: '/api/company/credit/dispute-counterparties', method: 'get' })
export const getCompanyDisputeRelationPicks = (params) =>
  request({ url: '/api/company/credit/dispute-relation-picks', method: 'get', params })