<template>
  <div class="query-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>通过文件哈希查询</h2>
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
        <el-form-item label="文件哈希" prop="fileHash">
          <el-input
            v-model="form.fileHash"
            placeholder="请输入文件哈希值"
            clearable
          >
            <template #append>
              <el-button @click="handleQuery" :loading="loading">
                查询
              </el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>

      <div v-if="queryResult" class="result-section">
        <h3>查询结果</h3>
        <el-descriptions :column="2" border class="result-descriptions">
          <el-descriptions-item label="申请编号">
            {{ queryResult.applicationNo || '-' }}
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
          <el-descriptions-item label="业务状态">
            {{ queryResult.bizStatus || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            {{ queryResult.auditStatus || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(queryResult.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatTime(queryResult.updatedAt) }}
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { queryByHash } from '@/api/copyright'

const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const queryResult = ref(null)
const queried = ref(false)

const form = reactive({
  fileHash: ''
})

const rules = {
  fileHash: [
    { required: true, message: '请输入文件哈希值', trigger: 'blur' }
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
        const res = await queryByHash(form.fileHash)
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

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString('zh-CN')
}

onMounted(() => {
  const hash = route.query.fileHash
  if (typeof hash === 'string' && hash.trim()) {
    form.fileHash = hash.trim()
    handleQuery()
  }
})
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
