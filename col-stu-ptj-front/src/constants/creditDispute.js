/** 与后端 CreditRelatedType 一致 */
export const CREDIT_RELATED_TYPES = [
  {
    value: 'NONE',
    label: '无关联记录',
    shortDesc: '仅通过文字说明事由，不绑定系统内某条申请/岗位/评价。'
  },
  {
    value: 'APPLICATION',
    label: '岗位申请记录',
    shortDesc:
      '对应一次投递记录：学生端为「我的投递」中的申请编号；企业端为候选人列表中的申请编号。便于管理员核对流程与状态。'
  },
  {
    value: 'JOB',
    label: '岗位信息',
    shortDesc: '对应一个具体岗位的主键编号（与岗位列表/详情中的岗位 ID 一致），表示争议与该岗位发布内容相关。'
  },
  {
    value: 'REVIEW',
    label: '互评记录',
    shortDesc: '对应一条双方评价记录的主键编号（评价中心里的评价 ID），与信用分或评价内容争议相关时选用。'
  }
]
