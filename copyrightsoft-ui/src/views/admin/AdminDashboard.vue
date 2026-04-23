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
      <el-tab-pane label="用户管理" name="users">
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
          </div>

          <el-table :data="users" border stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="nickname" label="昵称" width="150" />
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
            <el-table-column label="操作" fixed="right" width="200">
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
              placeholder="搜索软件名称/文件哈希"
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
          </div>

          <el-table :data="copyrights" border stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="softwareName" label="软件名称" width="200" />
            <el-table-column prop="fileHash" label="文件哈希" width="200" show-overflow-tooltip />
            <el-table-column prop="ownerAddress" label="拥有者地址" width="200" show-overflow-tooltip />
            <el-table-column prop="txHash" label="交易哈希" width="200" show-overflow-tooltip />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Check, Document, Search } from '@element-plus/icons-vue'
import { getUsers, updateUserStatus, resetPassword, getAllCopyrights, getStatistics } from '@/api/admin'

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
const copyrightPage = ref(1)
const copyrightPageSize = ref(10)
const copyrightTotal = ref(0)

const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    statistics.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
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
      keyword: copyrightKeyword.value
    })
    copyrights.value = res.data.records
    copyrightTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('加载版权记录失败')
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

const viewDetail = (record) => {
  window.open(`/query-id/${record.id}`, '_blank')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
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