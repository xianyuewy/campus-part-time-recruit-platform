<template>
  <div class="reviews-page reviews-page--student">
    <section class="page-hero">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <div class="page-hero-copy">
          <span class="page-hero-badge">
            <el-icon><Star /></el-icon>
            互评记录
          </span>
          <h1 class="page-hero-title">我的<span class="accent">评价</span></h1>
          <p class="page-hero-sub">查看您对企业的评价，以及企业给予您的反馈。</p>
        </div>
      </div>
    </section>

    <div class="page-body">
      <div class="table-card">
        <div class="tabs-bar">
          <el-tabs v-model="tab" class="styled-tabs" @tab-change="onTabChange">
            <el-tab-pane label="我发出的评价" name="given" />
            <el-tab-pane label="我收到的评价" name="received" />
          </el-tabs>
          <span v-if="rows.total" class="table-meta">共 {{ rows.total }} 条</span>
        </div>

        <el-table
          :data="rows.records"
          v-loading="loading"
          stripe
          class="data-table"
          empty-text="暂无评价记录"
        >
          <el-table-column label="评价人" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="name-cell">{{ row.fromDisplayName || row.fromUsername || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="被评价人" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="name-cell">{{ row.toDisplayName || row.toUsername || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="评分" width="140" align="center">
            <template #default="{ row }">
              <el-rate :model-value="Number(row.score) || 0" disabled show-score text-color="#409eff" />
            </template>
          </el-table-column>
          <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="时间" width="168" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="time-cell">{{ formatTime(row.createTime) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="rows.total"
            v-model:current-page="q.current"
            v-model:page-size="q.size"
            :page-sizes="[10, 20, 50]"
            @size-change="load"
            @current-change="load"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Star } from '@element-plus/icons-vue'
import { myGivenReviews, myReceivedReviews } from '../api/student'

const tab = ref('given')
const q = reactive({ current: 1, size: 10 })
const rows = reactive({ records: [], total: 0 })
const loading = ref(false)

const formatTime = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const load = async () => {
  loading.value = true
  try {
    const api = tab.value === 'given' ? myGivenReviews : myReceivedReviews
    const res = await api(q)
    if (res.success && res.data) Object.assign(rows, res.data)
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  q.current = 1
  load()
}

onMounted(load)
</script>

<style scoped lang="scss">
.reviews-page {
  margin: -8px -4px 0;
  min-height: 100%;
}

.page-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #1e4a6b 0%, #2d6a9a 45%, #409eff 100%);
  color: #fff;
}

.page-hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 90% 10%, rgba(255, 255, 255, 0.14), transparent 40%),
    radial-gradient(circle at 5% 85%, rgba(180, 220, 255, 0.22), transparent 50%);
  pointer-events: none;
}

.page-hero-inner {
  position: relative;
  padding: 28px 32px;
}

.page-hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.15);
  margin-bottom: 10px;
}

.page-hero-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;

  .accent {
    color: #b8dcff;
  }
}

.page-hero-sub {
  margin: 0;
  font-size: 14px;
  opacity: 0.92;
  max-width: 440px;
  line-height: 1.55;
}

.page-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.table-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 36px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef1f6;
  overflow: hidden;
}

.tabs-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px 0;
  border-bottom: 1px solid #f0f3f8;
  flex-wrap: wrap;
  gap: 8px;
}

.styled-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }
  :deep(.el-tabs__item) {
    font-weight: 500;
  }
}

.table-meta {
  font-size: 13px;
  color: #909399;
  padding-bottom: 8px;
}

.data-table {
  :deep(.el-table__header th) {
    background: #f8fafc !important;
    font-weight: 600;
  }
}

.name-cell {
  font-weight: 500;
  color: #303133;
}

.time-cell {
  font-size: 13px;
  color: #606266;
}

.pager {
  padding: 14px 20px 18px;
  display: flex;
  justify-content: flex-end;
}
</style>
