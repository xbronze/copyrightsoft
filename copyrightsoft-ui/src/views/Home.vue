<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <h1>基于区块链的软件版本溯源系统</h1>
          <p class="subtitle">安全、可信、不可篡改的版权存证平台</p>
        </div>
      </template>

      <!-- 管理员快捷入口 -->
      <div v-if="userStore.isAdmin" class="admin-quick-access">
        <el-alert
            title="管理员模式"
            type="success"
            :closable="false"
            show-icon
        >
          <template #default>
            <el-button type="primary" @click="$router.push('/admin/dashboard')">
              进入管理后台
            </el-button>
          </template>
        </el-alert>
      </div>
      <div v-else-if="userStore.role === 'AUDITOR'" class="admin-quick-access">
        <el-alert title="审核人员模式" type="info" :closable="false" show-icon />
      </div>
      <div class="features">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="hover" class="feature-card">
              <el-icon :size="50" color="#409EFF"><Upload /></el-icon>
              <h3>版权申请</h3>
              <p>{{ uploadSubtitle }}</p>
              <el-button type="primary" :disabled="!userStore.isDeveloper" @click="$router.push('/apply')">立即申请</el-button>
            </el-card>
          </el-col>

          <el-col :span="8">
            <el-card shadow="hover" class="feature-card">
              <el-icon :size="50" color="#67C23A"><Search /></el-icon>
              <h3>哈希查询</h3>
              <p>通过文件哈希查询版权信息</p>
              <el-button type="success" @click="$router.push('/query-hash')">立即查询</el-button>
            </el-card>
          </el-col>

          <el-col :span="8">
            <el-card shadow="hover" class="feature-card">
              <el-icon :size="50" color="#E6A23C"><Document /></el-icon>
              <h3>ID查询</h3>
              <p>通过记录ID查询版权详情</p>
              <el-button type="warning" @click="$router.push('/query-id')">立即查询</el-button>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="blockchain-info">
        <h2>技术特点</h2>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="区块链技术">
            <el-tag>FISCO BCOS</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="文件存储">
            <el-tag>MinIO</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数据加密">
            <el-tag>SHA-256</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="智能合约">
            <el-tag>Solidity</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="不可篡改">
            <el-tag type="success">✓</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="可追溯">
            <el-tag type="success">✓</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Upload, Search, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const uploadSubtitle = computed(() => (
  userStore.accountType === 'ENTERPRISE'
    ? '以企业主体上传软件，形成可审计的企业版权记录'
    : '上传软件文件，快速完成个人版权存证'
))
</script>

<style scoped>
.home-container {
  width: 100%;
  padding: 20px;
}

.welcome-card {
  margin-top: 20px;
}

.card-header {
  text-align: center;
}

.card-header h1 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 28px;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 16px;
}

.features {
  margin: 40px 0;
}

.feature-card {
  text-align: center;
  padding: 20px;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-card h3 {
  margin: 20px 0 10px 0;
  color: #303133;
}

.feature-card p {
  color: #909399;
  margin-bottom: 20px;
}

.blockchain-info {
  margin-top: 40px;
}

.blockchain-info h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}
</style>
