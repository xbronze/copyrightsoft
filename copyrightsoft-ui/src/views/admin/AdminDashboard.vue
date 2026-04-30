<template>
  <div class="admin-dashboard">
    <el-row :gutter="20" class="statistics-cards">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon active">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon copyrights">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalCopyrights || 0 }}</div>
              <div class="stat-label">版权记录总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="人员账号管理" name="users">
        <div class="tab-content">
          <div class="search-bar">
            <el-input
              v-model="userKeyword"
              placeholder="搜索用户名/昵称/邮箱"
              style="width: 300px"
              clearable
              @clear="loadUsers"
            >
              <template #append>
                <el-button @click="loadUsers">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            <el-button type="primary" @click="openUserDialog()">新增账号</el-button>
          </div>

          <el-table :data="users" border stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="nickname" label="昵称" width="150" />
            <el-table-column label="角色" width="170">
              <template #default="{ row }">
                <el-tag>{{ roleLabel(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="主体类型" width="140">
              <template #default="{ row }">
                {{ toAccountTypeText(row.accountType) }}
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" width="200" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="注册时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="300">
              <template #default="{ row }">
                <el-button
                  size="small"
                  :type="row.status === 1 ? 'warning' : 'success'"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" type="info" @click="handleResetPassword(row)">
                  重置密码
                </el-button>
                <el-button size="small" type="primary" plain @click="openUserDialog(row)">
                  编辑
                </el-button>
                <el-button size="small" type="danger" plain @click="handleDeleteUser(row)">
                  删除
                </el-button>
                <el-dropdown @command="(role) => openRoleDialog(row, role)">
                  <el-button size="small" type="primary">设置角色</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="INDIVIDUAL_DEVELOPER">个人开发者</el-dropdown-item>
                      <el-dropdown-item command="ENTERPRISE_DEVELOPER">企业开发者</el-dropdown-item>
                      <el-dropdown-item command="AUDITOR">审核员</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="userPage"
              v-model:page-size="userPageSize"
              :total="userTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadUsers"
              @current-change="loadUsers"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="版权记录管理" name="copyrights">
        <div class="tab-content">
          <div class="search-bar">
            <el-input
              v-model="copyrightKeyword"
              placeholder="搜索软件名称/文件哈希/申请编号"
              style="width: 300px"
              clearable
              @clear="loadCopyrights"
            >
              <template #append>
                <el-button @click="loadCopyrights">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            <el-select v-model="copyrightBizStatus" placeholder="业务状态" clearable style="width: 180px">
              <el-option label="上链成功" value="ONCHAIN_SUCCESS" />
              <el-option label="上链失败" value="ONCHAIN_FAILED" />
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="待人工复核" value="PENDING_REVIEW" />
            </el-select>
          </div>

          <el-table :data="copyrights" border stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="softwareName" label="软件名称" width="200" />
            <el-table-column prop="fileHash" label="文件哈希" width="200" show-overflow-tooltip />
            <el-table-column prop="ownerAddress" label="拥有者地址" width="200" show-overflow-tooltip />
            <el-table-column prop="txHash" label="交易哈希" width="200" show-overflow-tooltip />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column label="业务状态" width="120">
              <template #default="{ row }">
                {{ toBizStatusText(row.bizStatus) }}
              </template>
            </el-table-column>
            <el-table-column label="申请时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="120">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="viewDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="copyrightPage"
              v-model:page-size="copyrightPageSize"
              :total="copyrightTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadCopyrights"
              @current-change="loadCopyrights"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="userDialogVisible" :title="userDialog.id ? '编辑账号' : '新增账号'" width="560px">
      <el-form :model="userDialog" label-width="110px">
        <el-form-item label="用户名">
          <el-input v-model="userDialog.username" :disabled="!!userDialog.id" />
        </el-form-item>
        <el-form-item :label="userDialog.id ? '新密码(可空)' : '密码'">
          <el-input v-model="userDialog.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userDialog.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userDialog.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userDialog.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userDialog.role" style="width: 280px">
            <el-option label="个人开发者" value="INDIVIDUAL_DEVELOPER" />
            <el-option label="企业开发者" value="ENTERPRISE_DEVELOPER" />
            <el-option label="审核员" value="AUDITOR" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="userDialog.role === 'ENTERPRISE_DEVELOPER'" label="所属企业">
          <el-select
            v-model="userDialog.enterpriseId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入企业名称检索"
            :remote-method="searchEnterprises"
            :loading="enterpriseLoading"
            style="width: 320px"
          >
            <el-option
              v-for="item in enterpriseOptions"
              :key="item.id"
              :label="enterpriseOptionLabel(item)"
              :value="item.id"
              :disabled="isEnterpriseDisabled(item)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="userDialog.status" style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="userSubmitLoading" @click="submitUserDialog">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="设置用户角色" width="520px">
      <el-form label-width="110px">
        <el-form-item label="目标用户">
          <span>{{ roleDialog.username || '-' }}</span>
        </el-form-item>
        <el-form-item label="目标角色">
          <el-select v-model="roleDialog.role" style="width: 280px">
            <el-option label="个人开发者" value="INDIVIDUAL_DEVELOPER" />
            <el-option label="企业开发者" value="ENTERPRISE_DEVELOPER" />
            <el-option label="审核员" value="AUDITOR" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="roleDialog.role === 'ENTERPRISE_DEVELOPER'" label="所属企业">
          <el-select
            v-model="roleDialog.enterpriseId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入企业名称检索"
            :remote-method="searchEnterprises"
            :loading="enterpriseLoading"
            style="width: 320px"
          >
            <el-option
              v-for="item in enterpriseOptions"
              :key="item.id"
              :label="enterpriseOptionLabel(item)"
              :value="item.id"
              :disabled="isEnterpriseDisabled(item)"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitLoading" @click="submitRoleChange">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 管理后台主页。
 * 覆盖两类核心能力：账号管理（增删改、角色分配、状态切换）与版权记录总览。
 * 页面内额外维护“最近企业选择缓存”，减少管理员频繁分配企业账号时的重复检索成本。
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Check, Document, Search } from '@element-plus/icons-vue'
import {
  getUsers,
  updateUserStatus,
  resetPassword,
  getAllCopyrights,
  getStatistics,
  updateUserRole,
  getEnterpriseOptions,
  createUser,
  updateUser,
  deleteUser
} from '@/api/admin'
import { toAccountTypeText, toBizStatusText, toRoleText } from '@/utils/statusMap'

const activeTab = ref('users')
const statistics = ref({})

// 用户管理
const users = ref([])
const userKeyword = ref('')
const userPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)

// 版权管理
const copyrights = ref([])
const copyrightKeyword = ref('')
const copyrightBizStatus = ref('')
const copyrightPage = ref(1)
const copyrightPageSize = ref(10)
const copyrightTotal = ref(0)
const userDialogVisible = ref(false)
const userSubmitLoading = ref(false)
const roleDialogVisible = ref(false)
const enterpriseOptions = ref([])
const enterpriseLoading = ref(false)
const roleSubmitLoading = ref(false)
const recentEnterprises = ref([])
const RECENT_ENTERPRISES_KEY = 'admin_recent_enterprises'
const roleDialog = ref({
  userId: null,
  username: '',
  role: '',
  enterpriseId: null
})
const userDialog = ref({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  role: 'INDIVIDUAL_DEVELOPER',
  enterpriseId: null,
  status: 1
})

const loadRecentEnterprises = () => {
  try {
    const cache = localStorage.getItem(RECENT_ENTERPRISES_KEY)
    recentEnterprises.value = cache ? JSON.parse(cache) : []
  } catch (error) {
    recentEnterprises.value = []
  }
}

const saveRecentEnterprise = (enterprise) => {
  // 使用去重 + 头插策略保留最近使用企业，提升角色分配操作效率。
  if (!enterprise || !enterprise.id) return
  const merged = [enterprise, ...recentEnterprises.value.filter(item => item.id !== enterprise.id)].slice(0, 8)
  recentEnterprises.value = merged
  localStorage.setItem(RECENT_ENTERPRISES_KEY, JSON.stringify(merged))
}

const enterpriseOptionLabel = (item) => {
  const statusText = item.status === 1 ? '启用' : '禁用'
  const licensePart = item.licenseNo ? `，证照: ${item.licenseNo}` : ''
  return `${item.name}（ID: ${item.id}，${statusText}${licensePart}）`
}

const isEnterpriseDisabled = (item) => item?.status !== 1

const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    statistics.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const roleLabel = (role) => {
  return toRoleText(role)
}

const loadUsers = async () => {
  try {
    const res = await getUsers({
      page: userPage.value,
      size: userPageSize.value,
      keyword: userKeyword.value
    })
    users.value = res.data.records
    userTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const loadCopyrights = async () => {
  try {
    const res = await getAllCopyrights({
      page: copyrightPage.value,
      size: copyrightPageSize.value,
      keyword: copyrightKeyword.value,
      bizStatus: copyrightBizStatus.value
    })
    copyrights.value = res.data.records
    copyrightTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('加载版权记录失败')
  }
}

const openUserDialog = async (user) => {
  if (user) {
    userDialog.value = {
      id: user.id,
      username: user.username,
      password: '',
      nickname: user.nickname || '',
      email: user.email || '',
      phone: user.phone || '',
      role: user.role,
      enterpriseId: user.enterpriseId || null,
      status: user.status ?? 1
    }
  } else {
    userDialog.value = {
      id: null,
      username: '',
      password: '',
      nickname: '',
      email: '',
      phone: '',
      role: 'INDIVIDUAL_DEVELOPER',
      enterpriseId: null,
      status: 1
    }
  }
  if (userDialog.value.role === 'ENTERPRISE_DEVELOPER') {
    await searchEnterprises('')
  }
  userDialogVisible.value = true
}

const submitUserDialog = async () => {
  try {
    userSubmitLoading.value = true
    if (!userDialog.value.id && !userDialog.value.password) {
      ElMessage.error('新增账号必须设置密码')
      return
    }
    if (!userDialog.value.username) {
      ElMessage.error('用户名不能为空')
      return
    }
    if (userDialog.value.role === 'ENTERPRISE_DEVELOPER' && !userDialog.value.enterpriseId) {
      ElMessage.error('企业开发者必须选择所属企业')
      return
    }
    if (userDialog.value.id) {
      await updateUser(userDialog.value.id, userDialog.value)
      ElMessage.success('账号更新成功')
    } else {
      await createUser(userDialog.value)
      ElMessage.success('账号创建成功')
    }
    userDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '账号保存失败')
  } finally {
    userSubmitLoading.value = false
  }
}

const handleDeleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定删除账号 "${user.username}" 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteUser(user.id)
    ElMessage.success('账号删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleToggleStatus = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要${user.status === 1 ? '禁用' : '启用'}用户 "${user.username}" 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )

    const newStatus = user.status === 1 ? 0 : 1
    await updateUserStatus(user.id, newStatus)
    ElMessage.success('操作成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleResetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${user.username}" 的密码吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )

    const res = await resetPassword(user.id)
    ElMessageBox.alert(res.data, '新密码', { confirmButtonText: '确定' })
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '重置失败')
    }
  }
}

const searchEnterprises = async (keyword) => {
  try {
    if (!keyword) {
      // 空关键词先展示本地缓存，再融合后端结果，保证下拉框首屏可用。
      enterpriseOptions.value = [...recentEnterprises.value]
    }
    enterpriseLoading.value = true
    const res = await getEnterpriseOptions(keyword)
    const serverList = res.data || []
    const existingIds = new Set((enterpriseOptions.value || []).map(item => item.id))
    const merged = [...enterpriseOptions.value]
    serverList.forEach((item) => {
      if (!existingIds.has(item.id)) {
        merged.push(item)
      }
    })
    enterpriseOptions.value = merged
  } catch (error) {
    ElMessage.error(error.message || '加载企业列表失败')
  } finally {
    enterpriseLoading.value = false
  }
}

const openRoleDialog = async (user, role) => {
  roleDialog.value = {
    userId: user.id,
    username: user.username,
    role,
    enterpriseId: user.enterpriseId || null
  }
  roleDialogVisible.value = true
  roleSubmitLoading.value = false
  if (role === 'ENTERPRISE_DEVELOPER') {
    await searchEnterprises('')
  } else {
    enterpriseOptions.value = []
  }
}

const submitRoleChange = async () => {
  try {
    roleSubmitLoading.value = true
    if (roleDialog.value.role === 'ENTERPRISE_DEVELOPER' && !roleDialog.value.enterpriseId) {
      ElMessage.error('请选择企业')
      return
    }
    if (roleDialog.value.role === 'ENTERPRISE_DEVELOPER') {
      const selectedEnterprise = enterpriseOptions.value.find(item => item.id === roleDialog.value.enterpriseId)
      if (!selectedEnterprise || isEnterpriseDisabled(selectedEnterprise)) {
        ElMessage.error('所选企业不可用，请重新选择启用状态的企业')
        return
      }
    }

    await updateUserRole(
      roleDialog.value.userId,
      roleDialog.value.role,
      roleDialog.value.enterpriseId
    )
    if (roleDialog.value.role === 'ENTERPRISE_DEVELOPER') {
      // 角色切换为企业开发者时写入最近企业缓存，便于后续重复操作。
      const selected = enterpriseOptions.value.find(item => item.id === roleDialog.value.enterpriseId)
      saveRecentEnterprise(selected)
    }
    ElMessage.success('角色更新成功')
    roleDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '角色更新失败')
  } finally {
    roleSubmitLoading.value = false
  }
}

const viewDetail = (record) => {
  window.open(`/copyright/${record.applicationNo}`, '_blank')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadRecentEnterprises()
  loadStatistics()
  loadUsers()
  loadCopyrights()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-icon.users {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.active {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.copyrights {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.admin-tabs {
  background: white;
  padding: 20px;
  border-radius: 4px;
}

.tab-content {
  margin-top: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>