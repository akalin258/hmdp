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
      <!-- 添加店铺选择器 -->
      <el-select
        v-model="listQuery.shopId"
        placeholder="选择店铺"
        clearable
        class="filter-item"
      >
        <el-option
          v-for="shop in shopList"
          :key="shop.id"
          :label="shop.name"
          :value="shop.id"
        />
      </el-select>
      <el-button type="primary" @click="handleFilter">查询</el-button>
      
      <!-- 添加排序切换下拉菜单 -->
      <el-dropdown @command="handleSortCommand" trigger="click" class="sort-dropdown">
        <el-button type="info" plain>
          {{ getSortText() }} <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="default">默认排序</el-dropdown-item>
            <el-dropdown-item command="liked,desc">点赞数(多到少)</el-dropdown-item>
            <el-dropdown-item command="liked,asc">点赞数(少到多)</el-dropdown-item>
            <el-dropdown-item command="createTime,desc">发布时间(新到旧)</el-dropdown-item>
            <el-dropdown-item command="createTime,asc">发布时间(旧到新)</el-dropdown-item>
            <el-dropdown-item command="comments,desc">评论数(多到少)</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="加载中..."
      border
      fit
      highlight-current-row
      @sort-change="handleSortChange"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="userId" label="发布用户ID" width="120" />
      <el-table-column prop="shopId" label="关联商户ID" width="120" />
      <el-table-column 
        prop="liked" 
        label="点赞数" 
        width="80" 
        sortable="custom"
      />
      <el-table-column 
        prop="comments" 
        label="评论数" 
        width="80" 
        sortable="custom"
      />
      <el-table-column 
        prop="createTime" 
        label="发布时间" 
        width="160" 
        sortable="custom"
      />
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
            :src="formatImageUrl(getFirstImage(row.images))"
            style="width: 60px; height: 60px"
            :preview-src-list="getImagesList(row.images).map(formatImageUrl)"
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
            :src="formatImageUrl(image)"
            style="width: 120px; height: 120px; margin-right: 10px; margin-bottom: 10px"
            :preview-src-list="getImagesList(currentBlog.images).map(formatImageUrl)"
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
import { getShopList } from '@/api/shop'
import { NGINX_URL } from '@/utils/config'
import { ArrowDown } from '@element-plus/icons-vue'

// 博客列表数据
const list = ref([])
const total = ref(0)
const listLoading = ref(false)
const dialogVisible = ref(false)
const currentBlog = ref(null)
// 店铺列表
const shopList = ref([])

// 查询参数
const listQuery = reactive({
  current: 1,
  size: 10,
  title: '',
  shopId: '', // 添加shopId参数
  // 排序参数
  orderBy: '',
  order: ''
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
    ElMessage.error('获取探店笔记列表失败')
  } finally {
    listLoading.value = false
  }
}

// 获取店铺列表
const fetchShopList = async () => {
  try {
    const res = await getShopList({ size: 100 }) // 获取前100个店铺作为选项
    shopList.value = res.data.records
  } catch (error) {
    console.error('获取店铺列表失败', error)
    ElMessage.error('获取店铺列表失败')
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

// 处理排序变化 (表格列头排序)
const handleSortChange = ({ prop, order }) => {
  if (prop && order) {
    // 设置排序参数
    listQuery.orderBy = prop
    listQuery.order = order === 'ascending' ? 'asc' : 'desc'
  } else {
    // 取消排序
    listQuery.orderBy = ''
    listQuery.order = ''
  }
  getList()
}

// 处理下拉菜单排序选择
const handleSortCommand = (command) => {
  if (command === 'default') {
    listQuery.orderBy = ''
    listQuery.order = ''
  } else {
    const [field, order] = command.split(',')
    listQuery.orderBy = field
    listQuery.order = order
  }
  getList()
}

// 获取当前排序文本说明
const getSortText = () => {
  if (!listQuery.orderBy) {
    return '默认排序'
  }
  
  const fieldMap = {
    'liked': '点赞数',
    'comments': '评论数',
    'createTime': '发布时间'
  }
  
  const orderMap = {
    'asc': listQuery.orderBy === 'createTime' ? '(旧到新)' : '(少到多)',
    'desc': listQuery.orderBy === 'createTime' ? '(新到旧)' : '(多到少)'
  }
  
  return `${fieldMap[listQuery.orderBy]}${orderMap[listQuery.order]}`
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

// 工具函数 - 格式化图片URL
const formatImageUrl = (url) => {
  if (!url) return ''
  
  // 如果图片URL已经是完整URL（以http开头），则直接返回
  if (url.startsWith('http')) {
    return url
  }
  
  // 使用配置文件中定义的Nginx URL
  // 这样只需修改配置文件即可更改所有图片URL
  return `${NGINX_URL}${url}`
}

onMounted(() => {
  getList()
  fetchShopList() // 加载店铺列表
})
</script>

<style scoped>
.blog-container {
  padding: 20px;
}

.filter-container {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
}

.filter-item {
  width: 200px;
  margin-right: 10px;
}

.sort-dropdown {
  margin-left: auto;
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