<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ArrowDown, User, Document, SwitchButton, Management } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isAuthenticated)
const username = computed(() => userStore.username)
const normalizedRole = computed(() => (userStore.role === 'USER' ? 'INDIVIDUAL_DEVELOPER' : userStore.role))
const isAdmin = computed(() => normalizedRole.value === 'ADMIN')
const subjectText = computed(() => {
  if (userStore.accountType === 'ENTERPRISE') {
    return '企业开发者'
  }
  if (normalizedRole.value === 'AUDITOR') {
    return '审核人员'
  }
  if (normalizedRole.value === 'ADMIN') {
    return '管理员'
  }
  return '个人开发者'
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile/info')
  } else if (command === 'logout') {
    handleLogout()
  } else if (command === 'admin') {
    router.push('/admin/dashboard')
  } else if (command === 'audit') {
    router.push('/audit/review')
  }
}

// 判断是否在个人中心页面
const isProfilePage = computed(() => route.path.startsWith('/profile'))
// 判断是否在管理员页面
const isAdminPage = computed(() => route.path.startsWith('/admin'))
const isAuditPage = computed(() => route.path.startsWith('/audit'))
</script>

<template>
  <div id="app">
    <!-- 管理员页面布局 -->
    <el-container v-if="isAdminPage || isAuditPage">
      <el-aside width="250px" class="admin-sidebar">
        <div class="sidebar-header">
          <h2 @click="$router.push('/')" class="sidebar-logo">{{ isAuditPage ? '版权审核台' : '版权系统管理' }}</h2>
        </div>

        <el-menu
          :default-active="route.path"
          class="sidebar-menu"
          :router="true"
        >
          <el-menu-item v-if="isAdmin" index="/admin/dashboard">
            <el-icon><Management /></el-icon>
            <span>管理后台</span>
          </el-menu-item>
          <el-menu-item v-if="normalizedRole === 'AUDITOR' || normalizedRole === 'ADMIN'" index="/audit/review">
            <el-icon><Document /></el-icon>
            <span>审核工作台</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <el-button type="danger" @click="handleLogout" style="width: 100%">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-aside>

      <el-container>
        <el-header class="admin-header">
          <div class="admin-header-content">
            <span class="welcome-text">{{ isAuditPage ? '审核员' : subjectText }}：{{ username }}</span>
            <el-button @click="$router.push('/')" size="small">
              返回前台
            </el-button>
          </div>
        </el-header>

        <el-main class="admin-main">
          <RouterView />
        </el-main>
      </el-container>
    </el-container>

    <!-- 普通页面的头部导航 -->
    <el-container v-else-if="!isProfilePage">
      <el-header>
        <div class="header-content">
          <div class="left-section">
            <h1 @click="$router.push('/')" class="logo">区块链版权存证系统</h1>
          </div>

          <el-menu
            :default-active="$route.path"
            mode="horizontal"
            :router="true"
            background-color="#409EFF"
            text-color="#fff"
            active-text-color="#ffd04b"
            class="main-menu"
          >
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/apply" v-if="isLoggedIn">版权申请</el-menu-item>
            <el-sub-menu index="/query">
              <template #title>版权查询</template>
              <el-menu-item index="/query-hash">哈希查询</el-menu-item>
              <el-menu-item index="/query-id">ID查询</el-menu-item>
            </el-sub-menu>
          </el-menu>

          <div class="right-section">
            <div class="user-menu" v-if="isLoggedIn">
              <el-dropdown trigger="hover" @command="handleUserCommand">
                <span class="el-dropdown-link">
                  <el-icon><User /></el-icon>
                  {{ username }}
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="isAdmin" command="admin">
                      <el-icon><Management /></el-icon>
                      管理后台
                    </el-dropdown-item>
                    <el-dropdown-item v-if="normalizedRole === 'AUDITOR' || isAdmin" command="audit">
                      <el-icon><Document /></el-icon>
                      审核工作台
                    </el-dropdown-item>
                    <el-dropdown-item command="profile">
                      <el-icon><Document /></el-icon>
                      个人信息
                    </el-dropdown-item>
                    <el-dropdown-item command="logout" divided>
                      <el-icon><SwitchButton /></el-icon>
                      退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <div class="login-register" v-else>
              <el-button type="info" size="small" plain @click="$router.push('/login')">
                <el-icon><User /></el-icon>
                登录
              </el-button>
              <el-button type="primary" size="small" @click="$router.push('/register')">
                <el-icon><User /></el-icon>
                注册
              </el-button>
            </div>
          </div>
        </div>
      </el-header>

      <el-main>
        <RouterView />
      </el-main>

      <el-footer>
        <p>© 2026 基于区块链的软件版本溯源系统 | Powered by FISCO BCOS</p>
      </el-footer>
    </el-container>

    <!-- 个人中心页面的布局 -->
    <el-container v-else class="profile-layout">
      <el-aside width="250px" class="profile-sidebar">
        <div class="sidebar-header">
          <h2 @click="$router.push('/')" class="sidebar-logo">版权系统</h2>
        </div>

        <el-menu
          :default-active="route.path"
          class="sidebar-menu"
          :router="true"
        >
          <el-menu-item index="/profile/info">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/profile/records">
            <el-icon><Document /></el-icon>
            <span>我的版权记录</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <el-button type="danger" @click="handleLogout" style="width: 100%">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-aside>

      <el-container>
        <el-header class="profile-header">
          <div class="profile-header-content">
            <span class="welcome-text">欢迎，{{ subjectText }} {{ username }}</span>
            <el-button @click="$router.push('/')" size="small">
              返回首页
            </el-button>
          </div>
        </el-header>

        <el-main class="profile-main">
          <RouterView />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  width: 100%;
  height: 100%;
}

#app {
  width: 100%;
  min-height: 100vh;
}

/* 普通页面布局 */
.el-container {
  width: 100%;
  min-height: 100vh;
}

.el-header {
  background-color: #409EFF;
  padding: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  height: 60px !important;
}

.header-content {
  width: 100%;
  padding: 0 20px;
  display: flex;
  align-items: center;
  height: 100%;
  gap: 20px;
}

.left-section {
  display: flex;
  align-items: center;
}

.logo {
  color: #fff;
  margin: 0;
  cursor: pointer;
  font-size: 20px;
  white-space: nowrap;
}

.main-menu {
  border-bottom: none;
  flex: 1;
}

.right-section {
  display: flex;
  align-items: center;
}

.user-menu .el-dropdown-link {
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-menu .el-dropdown-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-menu .el-icon {
  font-size: 16px;
}

.login-register {
  display: flex;
  gap: 10px;
  align-items: center;
}

.login-register .el-button {
  display: flex;
  align-items: center;
  gap: 5px;
}

.el-main {
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px - 60px);
  padding: 0;
}

.el-footer {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid #e4e7ed;
  height: 60px !important;
}

.el-footer p {
  margin: 0;
  color: #909399;
}

/* 管理员页面布局 */
.admin-sidebar {
  background-color: #304156;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.admin-sidebar .sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-sidebar .sidebar-logo {
  color: #fff;
  margin: 0;
  cursor: pointer;
  font-size: 18px;
  text-align: center;
}

.admin-sidebar .sidebar-menu {
  flex: 1;
  border-right: none;
  background-color: transparent;
}

.admin-sidebar .sidebar-menu .el-menu-item {
  color: #bfcbd9;
}

.admin-sidebar .sidebar-menu .el-menu-item:hover {
  background-color: #263445 !important;
  color: #fff;
}

.admin-sidebar .sidebar-menu .el-menu-item.is-active {
  background-color: #409EFF !important;
  color: #fff;
}

.admin-sidebar .sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  height: 60px !important;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.admin-header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-header .welcome-text {
  font-size: 16px;
  color: #303133;
}

.admin-main {
  background-color: #f5f7fa;
  padding: 0;
}

/* 个人中心页面布局 */
.profile-layout {
  flex-direction: row;
}

.profile-sidebar {
  background-color: #304156;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-logo {
  color: #fff;
  margin: 0;
  cursor: pointer;
  font-size: 18px;
  text-align: center;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background-color: transparent;
}

.sidebar-menu .el-menu-item {
  color: #bfcbd9;
}

.sidebar-menu .el-menu-item:hover {
  background-color: #263445 !important;
  color: #fff;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #409EFF !important;
  color: #fff;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.profile-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  height: 60px !important;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.profile-header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text {
  font-size: 16px;
  color: #303133;
}

.profile-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
