// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: {
      requiresAuth: true,
      mainLayout: true
    },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'profile',
        name: 'ProfileCenter',
        component: () => import('../views/ProfileCenter.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('../views/Notifications.vue'),
        meta: { title: '消息通知' }
      },
      {
        path: 'jobs/:jobId',
        name: 'StudentJobDetail',
        component: () => import('../views/StudentJobDetail.vue'),
        meta: { title: '岗位详情', roles: ['STUDENT', 'COMPANY', 'ADMIN'] }
      },
      {
        path: 'jobs',
        name: 'StudentJobs',
        component: () => import('../views/StudentJobs.vue'),
        meta: { title: '岗位浏览', roles: ['STUDENT', 'COMPANY', 'ADMIN'] }
      },
      {
        path: 'credit',
        name: 'StudentCredit',
        component: () => import('../views/StudentCredit.vue'),
        meta: { title: '信用中心', roles: ['STUDENT'] }
      },
      {
        path: 'applications',
        name: 'StudentApplications',
        component: () => import('../views/StudentApplications.vue'),
        meta: { title: '我的投递', roles: ['STUDENT'] }
      },
      {
        path: 'resume',
        name: 'StudentResume',
        component: () => import('../views/StudentResume.vue'),
        meta: { title: '我的简历', roles: ['STUDENT'] }
      },
      {
        path: 'favorites',
        name: 'StudentFavorites',
        component: () => import('../views/StudentFavorites.vue'),
        meta: { title: '我的收藏', roles: ['STUDENT'] }
      },
      {
        path: 'reviews',
        name: 'StudentReviews',
        component: () => import('../views/StudentReviews.vue'),
        meta: { title: '我的评价', roles: ['STUDENT'] }
      },
      {
        path: 'company/profile',
        name: 'CompanyProfile',
        component: () => import('../views/CompanyProfile.vue'),
        meta: { title: '企业资料', roles: ['COMPANY'] }
      },
      {
        path: 'company/jobs',
        name: 'CompanyJobs',
        component: () => import('../views/CompanyJobs.vue'),
        meta: { title: '岗位管理', roles: ['COMPANY'] }
      },
      {
        path: 'company/applicants',
        name: 'CompanyApplicants',
        component: () => import('../views/CompanyApplicants.vue'),
        meta: { title: '候选人', roles: ['COMPANY'] }
      },
      {
        path: 'company/reviews',
        name: 'CompanyReviews',
        component: () => import('../views/CompanyReviews.vue'),
        meta: { title: '评价中心', roles: ['COMPANY'] }
      },
      {
        path: 'company/credit',
        name: 'CompanyCredit',
        component: () => import('../views/CompanyCredit.vue'),
        meta: { title: '信用中心', roles: ['COMPANY'] }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: {
      title: '管理后台',
      requiresAuth: true,
      roles: ['ADMIN']
    },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '管理首页' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/ProfileCenter.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'users',
        name: 'AdminUserAudit',
        component: () => import('../views/admin/UserAudit.vue'),
        meta: { title: '用户审核' }
      },
      {
        path: 'jobs',
        name: 'AdminJobAudit',
        component: () => import('../views/admin/JobAudit.vue'),
        meta: { title: '岗位审核' }
      },
      {
        path: 'user-manage',
        name: 'AdminUserManage',
        component: () => import('../views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'job-list',
        name: 'AdminJobList',
        component: () => import('../views/admin/JobList.vue'),
        meta: { title: '岗位管理' }
      },
      {
        path: 'credit',
        name: 'AdminCredit',
        component: () => import('../views/admin/CreditAdmin.vue'),
        meta: { title: '信用监管' }
      },
      {
        path: 'config',
        name: 'AdminConfig',
        component: () => import('../views/admin/SystemConfig.vue'),
        meta: { title: '系统配置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 简化的路由守卫
router.beforeEach((to, from, next) => {
  const titleMeta = to.matched
    .slice()
    .reverse()
    .find((r) => r.meta && r.meta.title)
  document.title = titleMeta?.meta?.title
    ? `${titleMeta.meta.title} - 大学生兼职系统`
    : '大学生兼职系统'

  if (to.matched.some((r) => r.meta && r.meta.requiresAuth)) {
    const token = localStorage.getItem('accessToken')
    if (!token) {
      next('/login')
      return
    }
  }

  const roleRecord = to.matched.find((r) => r.meta && r.meta.roles && r.meta.roles.length)
  if (roleRecord) {
    const userStore = useUserStore()
    const need = roleRecord.meta.roles
    const ok = need.some((role) => {
      if (role === 'ADMIN') return userStore.isAdmin
      if (role === 'STUDENT') return userStore.isStudent
      if (role === 'COMPANY') return userStore.isCompany
      return userStore.role === role
    })
    if (!ok) {
      next('/home')
      return
    }
  }

  if (to.path === '/login' || to.path === '/register') {
    const token = localStorage.getItem('accessToken')
    if (token) {
      next('/home')
      return
    }
  }

  next()
})

export default router
