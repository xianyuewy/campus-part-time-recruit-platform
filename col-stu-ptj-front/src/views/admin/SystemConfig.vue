<template>
  <div>
    <h2 class="page-title">系统配置</h2>
    <el-card v-loading="loading">
      <p class="hint">
        用于公告、首页轮播、信用规则说明等；修改后保存即可生效。
        <strong>首页轮播图</strong>：点击「编辑」后可用「上传轮播图」添加图片，也可在 JSON 文本框中手工维护 URL 列表。
      </p>
      <el-table :data="rows" border style="width: 100%">
        <el-table-column label="配置项" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="'系统键名：' + row.configKey" placement="top" :show-after="200">
              <span class="cfg-key-disp">{{ sysConfigKeyLabel(row.configKey) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" min-width="200" />
        <el-table-column label="内容" min-width="320">
          <template #default="{ row }">
            <template v-if="editKey === row.configKey">
              <div v-if="row.configKey === 'banner_images'" class="banner-editor">
                <el-upload
                  :show-file-list="false"
                  accept="image/jpeg,image/png,image/gif,image/webp,.jpg,.jpeg,.png,.gif,.webp"
                  :disabled="bannerUploading"
                  :http-request="onBannerUpload"
                >
                  <el-button type="primary" size="small" :loading="bannerUploading">上传轮播图</el-button>
                </el-upload>
                <p class="mini-hint">建议横向大图；单张不超过 5MB；支持 jpg / png / gif / webp。</p>
                <div v-if="bannerList.length" class="banner-previews">
                  <div v-for="(u, idx) in bannerList" :key="idx + u" class="banner-prev">
                    <img :src="fullAssetUrl(u)" alt="" />
                    <el-button type="danger" link size="small" @click="removeBanner(idx)">移除</el-button>
                  </div>
                </div>
                <el-input
                  v-model="editValue"
                  type="textarea"
                  :rows="4"
                  class="mt8"
                  placeholder='JSON 数组，如 ["/uploads/banners/xxx.jpg"]，与上方列表一致时保存会写入数据库'
                  @blur="syncBannerFromJson"
                />
              </div>
              <el-input v-else v-model="editValue" type="textarea" :rows="6" />
            </template>
            <span v-else class="preview">{{ truncate(row.configValue) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="editKey === row.configKey">
              <el-button type="primary" size="small" @click="saveRow(row)">保存</el-button>
              <el-button size="small" @click="cancelEdit">取消</el-button>
            </template>
            <el-button v-else type="primary" link @click="startEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
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
  if (!s) return ''
  return s.length > 120 ? s.slice(0, 120) + '…' : s
}

const parseBannerList = (json) => {
  if (!json || !json.trim()) return []
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

/** 与后台静态资源同源，便于管理端预览 */
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
    if (res.success && res.data) {
      rows.value = res.data
    }
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
      ElMessage.success('已加入列表，请点击「保存」写入配置')
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
      const arr = parseBannerList(editValue.value)
      value = JSON.stringify(arr)
    }
    await upsertSysConfig({
      configKey: row.configKey,
      configValue: value,
      remark: row.remark
    })
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
.page-title {
  margin: 0 0 16px;
}
.hint {
  color: #909399;
  font-size: 13px;
  margin-bottom: 16px;
  line-height: 1.6;
}
.preview {
  font-size: 13px;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
}
.cfg-key-disp {
  cursor: help;
}
.banner-editor {
  .mini-hint {
    font-size: 12px;
    color: #909399;
    margin: 8px 0 0;
  }
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
  align-items: flex-start;
  gap: 4px;
  img {
    width: 160px;
    height: 90px;
    object-fit: cover;
    border-radius: 8px;
    border: 1px solid #ebeef5;
    background: #f5f7fa;
  }
}
.mt8 {
  margin-top: 8px;
}
</style>
