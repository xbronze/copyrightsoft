<template>
  <div class="audit-review">
    <el-card>
      <template #header>
        <div class="header">
          <span>版权审核台</span>
          <div class="filters">
            <el-select v-model="auditStatus" placeholder="审核状态" style="width: 140px" clearable @change="loadRecords">
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
            <el-input v-model="keyword" placeholder="搜索软件名/哈希/主体" style="width: 280px" clearable @clear="loadRecords">
              <template #append>
                <el-button @click="loadRecords">搜索</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <el-table :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="softwareName" label="软件名称" min-width="180" />
        <el-table-column prop="subjectName" label="权利主体" min-width="140" />
        <el-table-column label="主体类型" width="120">
          <template #default="{ row }">
            {{ toSubjectTypeText(row.subjectType) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileHash" label="文件哈希" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.auditStatus)">{{ toAuditStatusText(row.auditStatus || 'PENDING') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" :disabled="row.auditStatus === 'APPROVED'" @click="submitAudit(row, 'APPROVED')">通过</el-button>
            <el-button size="small" type="danger" :disabled="row.auditStatus === 'REJECTED'" @click="submitAudit(row, 'REJECTED')">驳回</el-button>
          </template>
        </el-table-column>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAuditRecords, submitAuditAction } from '@/api/audit'
import { toAuditStatusText, toSubjectTypeText } from '@/utils/statusMap'

const records = ref([])
const keyword = ref('')
const auditStatus = ref('PENDING')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const statusType = (status) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

const loadRecords = async () => {
  try {
    const res = await getAuditRecords({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      auditStatus: auditStatus.value
    })
    records.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载审核列表失败')
  }
}

const submitAudit = async (row, status) => {
  try {
    const { value } = await ElMessageBox.prompt(
      status === 'APPROVED' ? '请输入通过备注（可选）' : '请输入驳回原因',
      status === 'APPROVED' ? '审核通过' : '审核驳回',
      {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputValue: '',
        inputValidator: (val) => status === 'REJECTED' && !val ? '驳回时请填写原因' : true
      }
    )
    await submitAuditAction(row.id, {
      auditStatus: status,
      auditComment: value
    })
    ElMessage.success('审核提交成功')
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审核提交失败')
    }
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.audit-review {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filters {
  display: flex;
  gap: 12px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
