<template>
  <div class="home-container">
    <div class="search-section">
      <h1>软件版权溯源查询</h1>
      <p class="subtitle">输入文件哈希，一键查询版权记录与链上凭证</p>
      <el-input
        v-model="fileHash"
        placeholder="请输入文件哈希值（SHA-256）"
        size="large"
        clearable
        @keyup.enter="goSearch"
      >
        <template #append>
          <el-button type="primary" @click="goSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const fileHash = ref('')

const goSearch = () => {
  router.push({
    path: '/query-hash',
    query: { fileHash: fileHash.value?.trim() || '' }
  })
}
</script>

<style scoped>
.home-container {
  min-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 24px;
  padding: 20px;
}

.search-section {
  width: 100%;
  max-width: 820px;
  text-align: center;
}

.search-section h1 {
  margin: 0 0 8px;
  font-size: 38px;
  color: #303133;
}

.subtitle {
  margin: 0 0 24px;
  color: #909399;
}

</style>
