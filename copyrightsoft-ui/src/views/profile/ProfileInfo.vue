<template>
  <div class="profile-info">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>个人信息</h3>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form
            ref="formRef"
            :model="userInfo"
            :rules="rules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userInfo.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="角色">
              <el-tag>{{ userInfo.role || 'USER' }}</el-tag>
            </el-form-item>

            <el-form-item label="注册时间">
              <el-text>{{ formatTime(userInfo.createdAt) }}</el-text>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleUpdate">保存修改</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="profile-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateProfile, changePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const activeTab = ref('basic')
const saving = ref(false)
const changingPassword = ref(false)

const userInfo = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  role: '',
  createdAt: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

onMounted(async () => {
  await loadUserInfo()
})

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    Object.assign(userInfo, res.data)
    console.log('用户信息:', res.data)
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

const handleUpdate = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        await updateProfile({
          nickname: userInfo.nickname,
          email: userInfo.email,
          phone: userInfo.phone
        })
        ElMessage.success('保存成功')
        // 如果昵称被修改，更新 store 中的用户名显示
        if (userInfo.nickname) {
          userStore.updateUserInfo(userInfo.nickname, userInfo.id)
        }
        await loadUserInfo()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.message || '保存失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const resetForm = () => {
  loadUserInfo()
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      try {
        await changePassword(passwordForm)
        ElMessage.success('密码修改成功，请重新登录')
        resetPasswordForm()
        // 延迟后退出登录
        setTimeout(() => {
          userStore.logout()
          window.location.href = '/login'
        }, 1500)
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error(error.message || '修改密码失败')
      } finally {
        changingPassword.value = false
      }
    }
  })
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.profile-info {
  max-width: 800px;
}

.card-header h3 {
  margin: 0;
  color: #303133;
}

.profile-form {
  margin-top: 20px;
}

.profile-form .el-form-item {
  max-width: 500px;
}
</style>
