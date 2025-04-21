<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="data-card">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
            </div>
          </template>
          <div class="card-body">
            <div class="card-value">{{ stats.userCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card">
          <template #header>
            <div class="card-header">
              <span>总店铺数</span>
            </div>
          </template>
          <div class="card-body">
            <div class="card-value">{{ stats.shopCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card">
          <template #header>
            <div class="card-header">
              <span>优惠券数量</span>
            </div>
          </template>
          <div class="card-body">
            <div class="card-value">{{ stats.voucherCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card">
          <template #header>
            <div class="card-header">
              <span>今日交易量</span>
            </div>
          </template>
          <div class="card-body">
            <div class="card-value">{{ stats.orderCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>系统概览</span>
        </div>
      </template>
      <div class="welcome-message">
        <h2>欢迎使用点评系统后台管理</h2>
        <p>在这里您可以管理系统的用户、店铺、优惠券等信息</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 模拟数据，实际应该通过API获取
const stats = ref({
  userCount: 1024,
  shopCount: 256,
  voucherCount: 128,
  orderCount: 64
})

onMounted(() => {
  // 这里可以调用API获取实际数据
  // 示例：getSystemStats().then(data => stats.value = data)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.data-card {
  margin-bottom: 20px;
  height: 180px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-body {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
}

.card-value {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
}

.chart-card {
  margin-top: 20px;
}

.welcome-message {
  padding: 40px 0;
  text-align: center;
}

.welcome-message h2 {
  margin-bottom: 20px;
  color: #303133;
}

.welcome-message p {
  color: #606266;
}
</style> 