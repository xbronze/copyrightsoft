<template>
  <div class="my-records">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>我的版权记录</h3>
          <el-button type="primary" @click="$router.push('/apply')">
            <el-icon><Plus /></el-icon>
            新增版权
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索软件名称/申请编号"
          clearable
          style="width: 300px"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="bizStatus" placeholder="业务状态" clearable style="width: 180px">
          <el-option label="上链成功" value="ONCHAIN_SUCCESS" />
          <el-option label="上链失败" value="ONCHAIN_FAILED" />
          <el-option label="已提交" value="SUBMITTED" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        :data="recordList"
        v-loading="loading"
        stripe
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="applicationNo" label="申请编号" min-width="170" />
        <el-table-column prop="softwareName" label="软件名称" min-width="150" />
        <el-table-column label="业务状态" width="140">
          <template #default="{ row }">
            {{ toBizStatusText(row.bizStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileHash" label="文件哈希" min-width="200">
          <template #default="{ row }">
            <el-text type="primary" style="word-break: break-all; font-size: 12px">
              {{ row.fileHash }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column prop="txHash" label="交易哈希" min-width="200">
          <template #default="{ row }">
            <el-text type="success" style="word-break: break-all; font-size: 12px">
              {{ row.txHash }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column prop="blockNumber" label="区块高度" width="120" />
        <el-table-column prop="createdAt" label="存证时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailDialogVisible"
      title="版权记录详情"
      width="900px"
      destroy-on-close
    >
      <el-skeleton :loading="detailLoading" animated>
        <template #default>
          <el-empty v-if="!detailData" description="暂无详情数据" />
          <el-descriptions v-else :column="2" border>
            <el-descriptions-item label="申请编号">{{ detailData.applicationNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="业务状态">{{ toBizStatusText(detailData.bizStatus) }}</el-descriptions-item>
            <el-descriptions-item label="软件名称">{{ detailData.softwareName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="归属主体">{{ detailData.subjectName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="文件哈希" :span="2">
              <el-text type="primary" style="word-break: break-all">{{ detailData.fileHash || '-' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="证据根哈希" :span="2">
              <el-text style="word-break: break-all">{{ detailData.evidenceRootHash || '-' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="元数据哈希" :span="2">
              <el-text style="word-break: break-all">{{ detailData.metadataHash || '-' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="交易哈希" :span="2">
              <el-text type="success" style="word-break: break-all">{{ detailData.txHash || '-' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="区块高度">{{ detailData.blockNumber ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">{{ toAuditStatusText(detailData.auditStatus) }}</el-descriptions-item>
            <el-descriptions-item label="风险等级">{{ toRiskLevelText(detailData.riskLevel) }}</el-descriptions-item>
            <el-descriptions-item label="相似度评分">{{ detailData.similarityScore ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="风险原因" :span="2">{{ detailData.riskReason || '-' }}</el-descriptions-item>
            <el-descriptions-item label="复核结论">{{ toReviewResultText(detailData.reviewResult) }}</el-descriptions-item>
            <el-descriptions-item label="复核备注">{{ detailData.reviewNote || '-' }}</el-descriptions-item>
            <el-descriptions-item label="软件描述" :span="2">{{ detailData.description || '无' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(detailData.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatTime(detailData.updatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-skeleton>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getMyRecords, queryByApplicationNo } from '@/api/copyright.js'
import { toAuditStatusText, toBizStatusText, toReviewResultText, toRiskLevelText } from '@/utils/statusMap'

const loading = ref(false)
const recordList = ref([])
const searchKeyword = ref('')
const bizStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

onMounted(() => {
  loadRecords()
})

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getMyRecords({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      bizStatus: bizStatus.value
    })
    recordList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载记录失败:', error)
    ElMessage.error('加载记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadRecords()
}

const handleReset = () => {
  searchKeyword.value = ''
  bizStatus.value = ''
  currentPage.value = 1
  loadRecords()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadRecords()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadRecords()
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString('zh-CN')
}

const viewDetail = async (row) => {
  if (!row?.applicationNo) {
    ElMessage.warning('该记录缺少申请编号，无法查看详情')
    return
  }
  detailDialogVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await queryByApplicationNo(row.applicationNo)
    detailData.value = res.data || null
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
  }
}
</script>

<style scoped>
.my-records {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #303133;
}

.search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>