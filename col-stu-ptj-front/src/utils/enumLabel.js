const MAP = {
  disputeStatus: {
    PENDING: '待处理',
    RESOLVED: '已处理',
    REJECTED: '已驳回'
  },
  creditRelatedType: {
    NONE: '无关联记录',
    APPLICATION: '岗位申请记录',
    JOB: '岗位信息',
    REVIEW: '互评记录'
  },
  jobStatus: {
    PENDING: '待审核',
    APPROVED: '已上架',
    REJECTED: '已驳回',
    OFFLINE: '已下架'
  },
  userRole: {
    STUDENT: '学生',
    COMPANY: '企业',
    ADMIN: '管理员'
  },
  authStatus: {
    UNAUTH: '待认证',
    PENDING: '审核中',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  },
  /**
   * 与后端百分制 0～100 的 CreditLevelCalculator 一致：
   * 90+ 优秀，75+ 良好，60+ 一般，未满 60 为待提升
   */
  creditLevel: {
    EXCELLENT: '优秀',
    GOOD: '良好',
    NORMAL: '一般',
    RISKY: '待提升'
  },
  /** 与 sys_config.config_key / SystemConfig.vue 约定一致，新增键时请在此补充 */
  sysConfigKey: {
    announcement: '平台公告',
    banner_images: '首页轮播图',
    credit_rules: '信用规则说明'
  }
}

const labelOf = (group, value) => {
  if (value === null || value === undefined || value === '') return '—'
  return MAP[group]?.[value] || value
}

export const disputeStatusLabel = (value) => labelOf('disputeStatus', value)
export const creditRelatedTypeLabel = (value) => labelOf('creditRelatedType', value)
export const jobStatusLabel = (value) => labelOf('jobStatus', value)
export const userRoleLabel = (value) => labelOf('userRole', value)
export const authStatusLabel = (value) => labelOf('authStatus', value)
export const creditLevelLabel = (value) => labelOf('creditLevel', value)

/** 系统配置项名称；未知键仍显示原 configKey 便于排查 */
export const sysConfigKeyLabel = (value) => {
  if (value === null || value === undefined || value === '') return '—'
  return MAP.sysConfigKey?.[value] || value
}
