<template>
  <div class="feature-page">
    <section class="page-hero page-hero--admin">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <span class="page-hero-badge"><el-icon><Setting /></el-icon> 平台运营</span>
        <h1 class="page-hero-title">系统<span class="accent">配置</span></h1>
        <p class="page-hero-sub">维护公告、首页轮播图与信用规则说明等配置项，保存后立即生效。</p>
      </div>
    </section>

    <div class="feature-body">
      <div v-loading="loading" class="table-card">
        <div class="table-card-head">
          <span>配置项列表</span>
        </div>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          class="config-hint"
          title="轮播图配置"
          description="点击「编辑」后可上传图片或手工维护 JSON 数组；保存后写入数据库。"
        />
        <el-table :data="rows" stripe class="data-table config-table">
          <el-table-column label="配置项" min-width="160">
            <template #default="{ row }">
              <el-tooltip :content="'键名：' + row.configKey" placement="top">
                <span class="cfg-key">{{ sysConfigKeyLabel(row.configKey) }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="160" show-overflow-tooltip />
          <el-table-column label="内容" min-width="320">
            <template #default="{ row }">
              <template v-if="editKey === row.configKey">
                <div v-if="row.configKey === 'banner_images'" class="banner-editor">
                  <el-upload
                    :show-file-list="false"
                    accept="image/jpeg,image/png,image/gif,image/webp"
                    :disabled="bannerUploading"
                    :http-request="onBannerUpload"
                  >
                    <el-button type="primary" size="small" round :loading="bannerUploading">上传轮播图</el-button>
                  </el-upload>
                  <p class="mini-hint">建议横向大图，单张 ≤ 5MB</p>
                  <div v-if="bannerList.length" class="banner-previews">
                    <div v-for="(u, idx) in bannerList" :key="idx + u" class="banner-prev">
                      <img :src="fullAssetUrl(u)" alt="" />
                      <el-button type="danger" link size="small" @click="removeBanner(idx)">移除</el-button>
                    </div>
                  </div>
                  <el-input
                    v-model="editValue"
                    type="textarea"
                    :rows="3"
                    class="mt8"
                    placeholder='JSON 数组，如 ["/uploads/banners/xxx.jpg"]'
                    @blur="syncBannerFromJson"
                  />
                </div>
                <el-input v-else v-model="editValue" type="textarea" :rows="5" />
              </template>
              <span v-else class="preview">{{ truncate(row.configValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right" align="center">
            <template #default="{ row }">
              <template v-if="editKey === row.configKey">
                <el-button type="primary" size="small" text @click="saveRow(row)">保存</el-button>
                <el-button size="small" text @click="cancelEdit">取消</el-button>
              </template>
              <el-button v-else type="primary" size="small" text @click="startEdit(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'
import { listSysConfig, upsertSysConfig, uploadAdminBannerImage } from '../../api/admin'
import { API_BASE_URL } from '../../utils/request'
import { sysConfigKeyLabel } from '../../utils/enumLabel'

const loading = ref(false)
const rows = ref([])
const editKey = ref('')
const editValue = ref('')
const bannerList = ref([])
const bannerUploading = ref(false)

const truncate = (s) => {
  if (!s) return '—'
  return s.length > 120 ? s.slice(0, 120) + '…' : s
}

const parseBannerList = (json) => {
  if (!json?.trim()) return []
  try {
    const v = JSON.parse(json)
    return Array.isArray(v) ? v.filter((u) => typeof u === 'string' && u.trim()) : []
  } catch {
    return []
  }
}

const syncEditValueFromBannerList = () => {
  editValue.value = JSON.stringify(bannerList.value, null, 0)
}

const fullAssetUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  const base = API_BASE_URL.replace(/\/$/, '')
  return path.startsWith('/') ? `${base}${path}` : `${base}/${path}`
}

const load = async () => {
  loading.value = true
  try {
    const res = await listSysConfig()
    if (res.success && res.data) rows.value = res.data
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const startEdit = (row) => {
  editKey.value = row.configKey
  editValue.value = row.configValue || ''
  if (row.configKey === 'banner_images') {
    bannerList.value = parseBannerList(editValue.value)
    syncEditValueFromBannerList()
  } else {
    bannerList.value = []
  }
}

const cancelEdit = () => {
  editKey.value = ''
  editValue.value = ''
  bannerList.value = []
}

const syncBannerFromJson = () => {
  if (editKey.value !== 'banner_images') return
  bannerList.value = parseBannerList(editValue.value)
}

const removeBanner = (idx) => {
  bannerList.value = bannerList.value.filter((_, i) => i !== idx)
  syncEditValueFromBannerList()
}

const onBannerUpload = async ({ file }) => {
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  bannerUploading.value = true
  try {
    const res = await uploadAdminBannerImage(fd)
    if (res.success && res.data?.url) {
      bannerList.value = [...bannerList.value, res.data.url]
      syncEditValueFromBannerList()
      ElMessage.success('已加入列表，请点击保存')
    }
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    bannerUploading.value = false
  }
}

const saveRow = async (row) => {
  try {
    let value = editValue.value
    if (row.configKey === 'banner_images') {
      value = JSON.stringify(parseBannerList(editValue.value))
    }
    await upsertSysConfig({ configKey: row.configKey, configValue: value, remark: row.remark })
    ElMessage.success('已保存')
    cancelEdit()
    await load()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
@use '../../styles/feature-page.scss';

.config-hint {
  margin: 16px 20px 0;
  border-radius: 10px;
}

.config-table {
  margin-top: 8px;
}

.cfg-key {
  font-weight: 600;
  color: #303133;
  cursor: help;
}

.preview {
  font-size: 13px;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
}

.banner-editor .mini-hint {
  font-size: 12px;
  color: #909399;
  margin: 8px 0 0;
}

.banner-previews {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.banner-prev {
  display: flex;
  flex-direction: column;
  gap: 4px;

  img {
    width: 160px;
    height: 90px;
    object-fit: cover;
    border-radius: 10px;
    border: 1px solid #eef1f6;
  }
}

.mt8 {
  margin-top: 8px;
}
</style>
