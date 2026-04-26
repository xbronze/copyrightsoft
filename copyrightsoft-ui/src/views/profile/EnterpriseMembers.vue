<template>
  <div class="enterprise-members">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>企业成员管理</h3>
          <el-button type="primary" @click="openCreateDialog">新增成员</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名/昵称/邮箱"
          clearable
          style="width: 320px"
          @clear="loadMembers"
        />
        <el-button type="primary" @click="loadMembers">搜索</el-button>
      </div>

      <el-table :data="members" v-loading="loading" stripe style="margin-top: 20px">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="enterpriseRole" label="企业角色" width="120" />
        <el-table-column prop="enterpriseLegalScope" label="法务范围" width="110" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-if="row.enterpriseRole === 'LEGAL'"
              size="small"
              type="success"
              @click="toggleLegalScope(row)"
            >
              切换法务范围
            </el-button>
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
          @size-change="loadMembers"
          @current-change="loadMembers"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑成员' : '新增成员'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" v-if="!form.id">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item :label="form.id ? '新密码(可空)' : '密码'">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="企业角色">
          <el-select v-model="form.enterpriseRole" style="width: 200px">
            <el-option label="开发者" value="DEVELOPER" />
            <el-option label="法务" value="LEGAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="法务范围" v-if="form.enterpriseRole === 'LEGAL'">
          <el-select v-model="form.enterpriseLegalScope" style="width: 200px">
            <el-option label="仅本人" value="SELF" />
            <el-option label="全企业" value="ALL" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createEnterpriseMember,
  getEnterpriseMembers,
  updateEnterpriseLegalScope,
  updateEnterpriseMember,
  updateEnterpriseMemberStatus
} from '@/api/enterprise'

const loading = ref(false)
const submitLoading = ref(false)
const members = ref([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  enterpriseRole: 'DEVELOPER',
  enterpriseLegalScope: 'SELF'
})

const loadMembers = async () => {
  loading.value = true
  try {
    const res = await getEnterpriseMembers({
      page: page.value,
      size: size.value,
      keyword: keyword.value
    })
    members.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载企业成员失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  form.value = {
    id: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    enterpriseRole: 'DEVELOPER',
    enterpriseLegalScope: 'SELF'
  }
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  form.value = {
    id: row.id,
    username: row.username,
    password: '',
    nickname: row.nickname || '',
    email: row.email || '',
    phone: row.phone || '',
    enterpriseRole: row.enterpriseRole || 'DEVELOPER',
    enterpriseLegalScope: row.enterpriseLegalScope || 'SELF'
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  try {
    submitLoading.value = true
    if (!form.value.id) {
      await createEnterpriseMember(form.value)
      ElMessage.success('成员创建成功')
    } else {
      await updateEnterpriseMember(form.value.id, form.value)
      ElMessage.success('成员更新成功')
    }
    dialogVisible.value = false
    loadMembers()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (row) => {
  try {
    await updateEnterpriseMemberStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态更新成功')
    loadMembers()
  } catch (error) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

const toggleLegalScope = async (row) => {
  try {
    const nextScope = row.enterpriseLegalScope === 'ALL' ? 'SELF' : 'ALL'
    await updateEnterpriseLegalScope(row.id, nextScope)
    ElMessage.success('法务范围更新成功')
    loadMembers()
  } catch (error) {
    ElMessage.error(error.message || '更新法务范围失败')
  }
}

onMounted(loadMembers)
</script>

<style scoped>
.enterprise-members {
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
