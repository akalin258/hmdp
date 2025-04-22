<template>
  <div class="blog-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.title"
        placeholder="笔记标题"
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
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="userId" label="发布用户ID" width="120" />
      <el-table-column prop="shopId" label="关联商户ID" width="120" />
      <el-table-column prop="liked" label="点赞数" width="80" />
      <el-table-column prop="comments" label="评论数" width="80" />
      <el-table-column prop="createTime" label="发布时间" width="160" />
      <el-table-column label="内容预览" width="200">
        <template #default="{ row }">
          <el-tooltip :content="row.content" placement="top">
            <span>{{ truncateContent(row.content) }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="图片预览" width="120">
        <template #default="{ row }">
          <el-image
            v-if="getFirstImage(row.images)"
            :src="getFirstImage(row.images)"
            style="width: 60px; height: 60px"
            :preview-src-list="getImagesList(row.images)"
          />
          <span v-else>无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 博客详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="探店笔记详情"
      width="800px"
    >
      <div v-if="currentBlog">
        <h2>{{ currentBlog.title }}</h2>
        <div class="blog-info">
          <span>发布用户ID: {{ currentBlog.userId }}</span> | 
          <span>关联商户ID: {{ currentBlog.shopId }}</span> | 
          <span>发布时间: {{ currentBlog.createTime }}</span>
        </div>
        <div class="blog-content">
          <p>{{ currentBlog.content }}</p>
        </div>
        <div class="blog-images" v-if="currentBlog.images">
          <el-image
            v-for="(image, index) in getImagesList(currentBlog.images)"
            :key="index"
            :src="image"
            style="width: 120px; height: 120px; margin-right: 10px; margin-bottom: 10px"
            :preview-src-list="getImagesList(currentBlog.images)"
          />
        </div>
        <div class="blog-stats">
          <span>点赞数: {{ currentBlog.liked }}</span> | 
          <span>评论数: {{ currentBlog.comments }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBlogList, getBlogDetail, deleteBlog } from '@/api/blog'

// 博客列表数据
const list = ref([])
const total = ref(0)
const listLoading = ref(false)
const dialogVisible = ref(false)
const currentBlog = ref(null)

// 查询参数
const listQuery = reactive({
  current: 1,
  size: 10,
  title: ''
})

// 获取博客列表
const getList = async () => {
  listLoading.value = true
  try {
    const res = await getBlogList(listQuery)
    list.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取探店笔记列表失败', error)
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

// 查看博客详情
const handleView = async (row) => {
  try {
    const res = await getBlogDetail(row.id)
    currentBlog.value = res.data
    dialogVisible.value = true
  } catch (error) {
    console.error('获取探店笔记详情失败', error)
  }
}

// 删除博客
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除该探店笔记吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBlog(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除探店笔记失败', error)
    }
  }).catch(() => {})
}

// 工具函数 - 截断内容
const truncateContent = (content) => {
  if (!content) return ''
  return content.length > 30 ? content.substring(0, 30) + '...' : content
}

// 工具函数 - 获取第一张图片
const getFirstImage = (images) => {
  if (!images) return ''
  const imagesList = images.split(',')
  return imagesList[0]
}

// 工具函数 - 获取所有图片列表
const getImagesList = (images) => {
  if (!images) return []
  return images.split(',')
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.blog-container {
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

.blog-info {
  color: #666;
  margin-bottom: 15px;
}

.blog-content {
  margin-bottom: 15px;
  white-space: pre-wrap;
}

.blog-images {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.blog-stats {
  color: #666;
}
</style> 