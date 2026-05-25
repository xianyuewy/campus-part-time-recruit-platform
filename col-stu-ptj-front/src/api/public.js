import request from '../utils/request'

export const getPublicConfig = () => request({ url: '/api/public/config', method: 'get' })
