import { defineStore } from 'pinia'
import { getUnreadNotificationCount } from '../api/notification'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    unreadCount: 0
  }),
  actions: {
    async fetchUnread() {
      const token = localStorage.getItem('accessToken')
      if (!token) {
        this.unreadCount = 0
        return
      }
      try {
        const res = await getUnreadNotificationCount()
        if (res.success) {
          this.unreadCount = Number(res.data?.count) || 0
        }
      } catch {
        // ignore
      }
    },
    setUnread(n) {
      this.unreadCount = Math.max(0, Number(n) || 0)
    }
  }
})
