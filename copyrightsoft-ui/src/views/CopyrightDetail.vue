<template>
  <div class="detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>软件版权详情</h2>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-skeleton :loading="loading" animated>
        <template #default>
          <el-empty v-if="!detail" description="未找到版权记录" />
          <el-descriptions v-else :column="2" border>
            <el-descriptions-item label="申请编号">{{ detail.applicationNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="业务状态">{{ toBizStatusText(detail.bizStatus) }}</el-descriptions-item>
            <el-descriptions-item label="软件名称">{{ detail.softwareName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="归属主体">{{ detail.subjectName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="文件哈希" :span="2">
              <el-text type="primary" style="word-break: break-all">{{ detail.fileHash || '-' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="证据根哈希" :span="2">
              <el-text style="word-break: break-all">{{ detail.evidenceRootHash || '-' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="元数据哈希" :span="2">
              <el-text style="word-break: break-all">{{ detail.metadataHash || '-' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="拥有者地址" :span="2">
              <el-text style="word-break: break-all">{{ detail.ownerAddress || '-' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="交易哈希" :span="2">
              <el-text type="success" style="word-break: break-all">{{ detail.txHash || '-' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="区块高度">{{ detail.blockNumber ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">{{ toAuditStatusText(detail.auditStatus) }}</el-descriptions-item>
            <el-descriptions-item label="软件描述" :span="2">{{ detail.description || '无' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(detail.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDate(detail.updatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-skeleton>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { queryByApplicationNo } from '@/api/copyright'
import { toAuditStatusText, toBizStatusText } from '@/utils/statusMap'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadDetail = async () => {
  const applicationNo = route.params.applicationNo
  if (!applicationNo) return
  loading.value = true
  try {
    const res = await queryByApplicationNo(applicationNo)
    detail.value = res.data
  } catch (error) {
    detail.value = null
    ElMessage.error(error.message || '加载详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-container {
  width: 100%;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}
</style>
