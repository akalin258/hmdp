<template>
  <div class="user-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.nickName"
        placeholder="用户昵称"
        class="filter-item"
        clearable
        @keyup.enter="handleFilter"
      />
      <el-input
        v-model="listQuery.phone"
        placeholder="手机号码"
        class="filter-item"
        clearable
        @keyup.enter="handleFilter"
      />
      <el-button type="primary" @click="handleFilter">查询</el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="加载中..."
      border
      fit
      highlight-current-row
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="nickName" label="昵称" width="120" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="icon" label="头像" width="100">
        <template #default="{ row }">
          <el-avatar 
            :src="row.icon ? formatImageUrl(row.icon) : '/default-avatar.png'" 
            @error="() => true"
          >
            <el-icon><User /></el-icon>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button 
            type="warning" 
            size="small" 
            @click="handleResetPassword(row)"
          >
            重置密码
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="listQuery.current"
      :page-size="listQuery.size"
      :total="total"
      :page-sizes="[10, 20, 30, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, resetUserPassword } from '@/api/user'
import { NGINX_URL } from '@/utils/config'
import { User } from '@element-plus/icons-vue'

// 用户列表数据
const list = ref([])
const total = ref(0)
const listLoading = ref(false)

// 查询参数
const listQuery = reactive({
  current: 1,
  size: 10,
  nickName: '',
  phone: ''
})

// 格式化图片URL
const formatImageUrl = (url) => {
  if (!url) return ''
  
  // 如果图片URL已经是完整URL（以http开头），则直接返回
  if (url.startsWith('http')) {
    return url
  }
  
  // 使用配置文件中定义的Nginx URL
  return `${NGINX_URL}${url}`
}

// 获取用户列表
const getList = async () => {
  listLoading.value = true
  try {
    const res = await getUserList(listQuery)
    list.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    listLoading.value = false
  }
}

// 查询按钮
const handleFilter = () => {
  listQuery.current = 1
  getList()
}

// 页码改变
const handleSizeChange = (val) => {
  listQuery.size = val
  getList()
}

const handleCurrentChange = (val) => {
  listQuery.current = val
  getList()
}

// 重置用户密码
const handleResetPassword = (row) => {
  ElMessageBox.confirm(`确认重置用户 ${row.nickName} 的密码吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await resetUserPassword(row.id)
      ElMessage.success('密码重置成功，新密码为：123456')
    } catch (error) {
      console.error('重置密码失败', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.filter-container {
  display: flex;
  margin-bottom: 20px;
}

.filter-item {
  width: 200px;
  margin-right: 10px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style> 