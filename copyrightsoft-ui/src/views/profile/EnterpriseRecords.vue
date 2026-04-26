<template>
  <div class="enterprise-records">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>企业版权记录</h3>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索软件名称/申请编号"
          clearable
          style="width: 320px"
          @clear="handleSearch"
        />
        <el-select v-model="bizStatus" placeholder="业务状态" clearable style="width: 180px">
          <el-option label="上链成功" value="ONCHAIN_SUCCESS" />
          <el-option label="上链失败" value="ONCHAIN_FAILED" />
          <el-option label="待复核" value="PENDING_REVIEW" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="records" v-loading="loading" stripe style="margin-top: 20px">
        <el-table-column prop="applicationNo" label="申请编号" min-width="170" />
        <el-table-column prop="softwareName" label="软件名称" min-width="180" />
        <el-table-column prop="subjectName" label="企业主体" min-width="140" />
        <el-table-column label="业务状态" width="140">
          <template #default="{ row }">
            {{ toBizStatusText(row.bizStatus) }}
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="110">
          <template #default="{ row }">
            {{ toRiskLevelText(row.riskLevel) }}
          </template>
        </el-table-column>
        <el-table-column prop="similarityScore" label="相似度" width="90" />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getEnterpriseRecords } from '@/api/copyright'
import { toBizStatusText, toRiskLevelText } from '@/utils/statusMap'

const loading = ref(false)
const records = ref([])
const keyword = ref('')
const bizStatus = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getEnterpriseRecords({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      bizStatus: bizStatus.value
    })
    records.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载企业版权记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadRecords()
}

onMounted(loadRecords)
</script>

<style scoped>
.enterprise-records {
  width: 100%;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
