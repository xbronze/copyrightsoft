<template>
  <div class="query-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>通过记录ID查询</h2>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="query-form"
      >
        <el-form-item label="记录ID" prop="id">
          <el-input-number
            v-model="form.id"
            :min="1"
            placeholder="请输入记录ID"
            style="width: 100%"
          >
            <template #append>
              <el-button @click="handleQuery" :loading="loading">
                查询
              </el-button>
            </template>
          </el-input-number>
        </el-form-item>
      </el-form>

      <div v-if="queryResult" class="result-section">
        <h3>查询结果</h3>
        <el-descriptions :column="2" border class="result-descriptions">
          <el-descriptions-item label="记录ID">
            {{ queryResult.id }}
          </el-descriptions-item>
          <el-descriptions-item label="软件名称">
            {{ queryResult.softwareName }}
          </el-descriptions-item>
          <el-descriptions-item label="文件哈希" :span="2">
            <el-text type="primary" style="word-break: break-all">
              {{ queryResult.fileHash }}
            </el-text>
          </el-descriptions-item>
          <el-descriptions-item label="版权拥有者" :span="2">
            <el-text style="word-break: break-all">
              {{ queryResult.ownerAddress }}
            </el-text>
          </el-descriptions-item>
          <el-descriptions-item label="软件描述" :span="2">
            {{ queryResult.description || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="交易哈希" :span="2">
            <el-text type="success" style="word-break: break-all">
              {{ queryResult.txHash }}
            </el-text>
          </el-descriptions-item>
          <el-descriptions-item label="区块高度">
            {{ queryResult.blockNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="文件大小">
            {{ formatFileSize(queryResult.fileSize) }}
          </el-descriptions-item>
          <el-descriptions-item label="文件类型">
            {{ queryResult.fileType || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="存证时间">
            {{ formatTime(queryResult.timestamp) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ queryResult.createTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-empty
        v-else-if="queried"
        description="未找到相关版权记录"
        :image-size="200"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { queryById } from '@/api/copyright'

const formRef = ref(null)
const loading = ref(false)
const queryResult = ref(null)
const queried = ref(false)

const form = reactive({
  id: null
})

const rules = {
  id: [
    { required: true, message: '请输入记录ID', trigger: 'blur' }
  ]
}

const handleQuery = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      queried.value = false
      queryResult.value = null

      try {
        const res = await queryById(form.id)
        queryResult.value = res.data
        queried.value = true
        ElMessage.success('查询成功！')
      } catch (error) {
        console.error('查询失败:', error)
        queried.value = true
      } finally {
        loading.value = false
      }
    }
  })
}

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp * 1000)
  return date.toLocaleString('zh-CN')
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}
</script>

<style scoped>
.query-container {
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

.query-form {
  margin-top: 20px;
}

.result-section {
  margin-top: 30px;
}

.result-section h3 {
  margin-bottom: 15px;
  color: #303133;
}

.result-descriptions {
  margin-top: 15px;
}
</style>
