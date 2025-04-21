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
          <el-avatar :src="row.icon || '/default-avatar.png'" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status ? 'success' : 'danger'">
            {{ row.status ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button 
            :type="row.status ? 'danger' : 'success'" 
            size="small" 
            @click="handleStatusChange(row)"
          >
            {{ row.status ? '禁用' : '启用' }}
          </el-button>
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
import { getUserList, updateUserStatus, resetUserPassword } from '@/api/user'

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

// 更改用户状态
const handleStatusChange = (row) => {
  const statusText = row.status ? '禁用' : '启用'
  ElMessageBox.confirm(`确认${statusText}用户 ${row.nickName} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateUserStatus(row.id, !row.status)
      ElMessage.success(`${statusText}成功`)
      getList()
    } catch (error) {
      console.error(`${statusText}用户失败`, error)
    }
  }).catch(() => {})
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
      ElMessage.success('密码重置成功')
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