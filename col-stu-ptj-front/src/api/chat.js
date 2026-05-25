import request from '../utils/request'

/** 投递沟通消息列表 */
export const getApplicationChat = (applicationId, params) =>
  request({ url: `/api/chat/application/${applicationId}`, method: 'get', params })

/** 发送一条消息 */
export const sendApplicationChat = (applicationId, data) =>
  request({ url: `/api/chat/application/${applicationId}`, method: 'post', data })
