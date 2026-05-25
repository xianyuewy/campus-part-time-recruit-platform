import request from '../utils/request'

export const pageNotifications = (params) =>
  request({ url: '/api/notifications', method: 'get', params })

export const getUnreadNotificationCount = () =>
  request({ url: '/api/notifications/unread-count', method: 'get' })

export const markNotificationRead = (id) =>
  request({ url: `/api/notifications/${id}/read`, method: 'post' })

export const markAllNotificationsRead = () =>
  request({ url: '/api/notifications/read-all', method: 'post' })
