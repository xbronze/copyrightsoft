<template>
  <div class="apply-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>版权存证申请</h2>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="apply-form"
      >
        <el-form-item label="软件名称" prop="softwareName">
          <el-input
            v-model="form.softwareName"
            placeholder="请输入软件名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="软件描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入软件描述（可选）"
          />
        </el-form-item>

        <el-form-item label="上传文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持任意格式文件，文件大小不超过100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="submitForm"
            style="width: 200px"
          >
            {{ loading ? '存证中...' : '提交存证' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <div v-if="result" class="result-section">
        <h3>申请结果</h3>
        <el-alert
          :title="resultAlertTitle"
          :type="resultAlertType"
          :closable="false"
          show-icon
        />
        <el-descriptions :column="1" border class="result-descriptions">
          <el-descriptions-item label="申请编号">
            <el-text type="primary" style="word-break: break-all">
              {{ result.applicationNo }}
            </el-text>
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-text type="warning">
              {{ toBizStatusText(result.status) }}
            </el-text>
          </el-descriptions-item>
          <el-descriptions-item v-if="result.errorMessage" label="失败原因">
            <el-text type="danger" style="word-break: break-all">
              {{ result.errorMessage }}
            </el-text>
          </el-descriptions-item>
          <el-descriptions-item label="交易哈希">
            <el-text type="primary" style="word-break: break-all">
              {{ result.txHash || '-' }}
            </el-text>
          </el-descriptions-item>
        </el-descriptions>
        <el-button type="primary" @click="$router.push('/query-hash')" style="margin-top: 20px">
          查询存证信息
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 版权申请页面。
 * 提交后先拿到申请编号，再根据申请状态轮询查询，直到进入终态（成功/失败）再停止。
 */
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { submitApplication, getApplicationStatus } from '@/api/copyright'
import { toBizStatusText } from '@/utils/statusMap'

const router = useRouter()
const formRef = ref(null)
const uploadRef = ref(null)
const loading = ref(false)
const result = ref(null)
const pollingTimer = ref(null)

const form = reactive({
  softwareName: '',
  description: '',
  file: null
})

const rules = {
  softwareName: [
    { required: true, message: '请输入软件名称', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请上传文件', trigger: 'change' }
  ]
}

const handleFileChange = (file) => {
  form.file = file.raw
}

const terminalStatuses = ['ONCHAIN_SUCCESS', 'ONCHAIN_FAILED']

const resultAlertType = computed(() => {
  if (!result.value) return 'info'
  if (result.value.status === 'ONCHAIN_SUCCESS') return 'success'
  if (result.value.status === 'ONCHAIN_FAILED') return 'error'
  return 'info'
})

const resultAlertTitle = computed(() => {
  if (!result.value) return ''
  if (result.value.status === 'ONCHAIN_SUCCESS') return '申请已提交并完成上链！'
  if (result.value.status === 'ONCHAIN_FAILED') return '申请上链失败，请查看失败原因'
  return '申请处理中，正在轮询状态...'
})

const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

const startPollingStatus = (applicationNo) => {
  stopPolling()
  // 使用固定间隔轮询以平衡时效与后端压力；组件卸载或命中终态时主动停止。
  pollingTimer.value = setInterval(async () => {
    try {
      const res = await getApplicationStatus(applicationNo)
      result.value = {
        ...result.value,
        ...res.data
      }
      if (terminalStatuses.includes(result.value.status)) {
        stopPolling()
      }
    } catch (error) {
      console.error('轮询申请状态失败:', error)
      stopPolling()
    }
  }, 3000)
}

const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const formData = new FormData()
        formData.append('file', form.file)
        formData.append('softwareName', form.softwareName)
        if (form.description) {
          formData.append('description', form.description)
        }

        const res = await submitApplication(formData)
        result.value = res.data
        if (!terminalStatuses.includes(result.value.status)) {
          // 对 PENDING_REVIEW / AUTO_CHECKED 等中间态继续追踪，减少用户手动刷新成本。
          startPollingStatus(result.value.applicationNo)
        }
        ElMessage.success('版权申请提交成功！')
      } catch (error) {
        console.error('申请失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  stopPolling()
  formRef.value?.resetFields()
  uploadRef.value?.clearFiles()
  form.file = null
  result.value = null
}

onBeforeUnmount(() => {
  stopPolling()
})
</script>

<style scoped>
.apply-container {
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

.apply-form {
  margin-top: 20px;
}

.result-section {
  margin-top: 20px;
}

.result-section h3 {
  margin-bottom: 15px;
  color: #303133;
}

.result-descriptions {
  margin-top: 15px;
}
</style>
