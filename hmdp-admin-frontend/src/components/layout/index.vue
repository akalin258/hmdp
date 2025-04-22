<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container">
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item index="/shop">
          <el-icon><Shop /></el-icon>
          <span>店铺管理</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/blog">
          <el-icon><Document /></el-icon>
          <span>探店笔记</span>
        </el-menu-item>
        <el-menu-item index="/voucher">
          <el-icon><Ticket /></el-icon>
          <span>优惠券管理</span>
        </el-menu-item>
      </el-menu>
    </div>
    
    <!-- 主区域 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="navbar">
        <div class="right-menu">
          <el-dropdown>
            <span class="avatar-wrapper">
              管理员 <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 内容区 -->
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataLine, Shop, User, Ticket, ArrowDown, Document } from '@element-plus/icons-vue'
import { logout as logoutApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const logout = async () => {
  try {
    await logoutApi()
  } catch (error) {
    console.error('登出失败', error)
  }
  localStorage.removeItem('isLoggedIn')
  router.push('/login')
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  background: #304156;
  overflow-y: auto;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.right-menu {
  margin-left: auto;
}

.avatar-wrapper {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f0f2f5;
}
</style> 