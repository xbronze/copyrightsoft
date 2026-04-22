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
          placeholder="搜索软件名称"
          clearable
          style="width: 300px"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
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
        <el-table-column prop="softwareName" label="软件名称" min-width="150" />
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getMyRecords } from '@/api/copyright.js'

const router = useRouter()
const loading = ref(false)
const recordList = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadRecords()
})

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getMyRecords({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
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

const viewDetail = (row) => {
  router.push(`/query-id/${row.id}`)
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