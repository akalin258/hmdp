<template>
  <div class="shop-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        placeholder="店铺名称"
        class="filter-item"
        clearable
        @keyup.enter="handleFilter"
      />
      <el-select
        v-model="listQuery.typeId"
        placeholder="店铺类型"
        clearable
        class="filter-item"
      >
        <el-option
          v-for="item in shopTypes"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-button type="primary" @click="handleFilter">查询</el-button>
      <el-button type="success" @click="handleAdd">新增店铺</el-button>
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
      <el-table-column prop="name" label="店铺名称" min-width="150" />
      <el-table-column prop="typeName" label="类型" width="100" />
      <el-table-column prop="area" label="地区" width="120" />
      <el-table-column prop="address" label="详细地址" min-width="200" />
      <el-table-column prop="avgPrice" label="人均价格" width="100">
        <template #default="{ row }">
          <span>¥{{ row.avgPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="评分" width="100">
        <template #default="{ row }">
          <el-rate v-model="row.displayScore" disabled text-color="#ff9900" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 店铺编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增店铺' : '编辑店铺'"
      width="600px"
    >
      <el-form
        ref="shopFormRef"
        :model="shopForm"
        :rules="shopRules"
        label-width="100px"
      >
        <el-form-item label="店铺名称" prop="name">
          <el-input v-model="shopForm.name" />
        </el-form-item>
        <el-form-item label="店铺类型" prop="typeId">
          <el-select v-model="shopForm.typeId" placeholder="请选择店铺类型">
            <el-option
              v-for="item in shopTypes"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="地区" prop="area">
          <el-input v-model="shopForm.area" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="shopForm.address" />
        </el-form-item>
        <el-form-item label="人均价格" prop="avgPrice">
          <el-input-number v-model="shopForm.avgPrice" :min="0" />
        </el-form-item>
        <el-form-item label="店铺图片" prop="images">
          <el-input v-model="shopForm.images" placeholder="图片URL（可选）" />
        </el-form-item>
        <el-form-item label="经度" prop="x">
          <el-input-number v-model="shopForm.x" :precision="6" :step="0.000001" :min="0" />
        </el-form-item>
        <el-form-item label="纬度" prop="y">
          <el-input-number v-model="shopForm.y" :precision="6" :step="0.000001" :min="0" />
        </el-form-item>
        <el-form-item label="销量" prop="sold">
          <el-input-number v-model="shopForm.sold" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="评论数" prop="comments">
          <el-input-number v-model="shopForm.comments" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-rate 
            v-model="shopForm.displayScore" 
            :max="5" 
            :texts="['1分', '2分', '3分', '4分', '5分']" 
            show-text 
            @change="handleScoreChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShopForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getShopList, getShopDetail, addShop, updateShop, deleteShop, getShopTypeList } from '@/api/shop'

// 店铺列表数据
const list = ref([])
const total = ref(0)
const listLoading = ref(false)
const shopTypes = ref([])

// 查询参数
const listQuery = reactive({
  current: 1,
  size: 10,
  name: '',
  typeId: ''
})

// 编辑表单数据
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const shopFormRef = ref(null)
const shopForm = reactive({
  id: undefined,
  name: '',
  typeId: '',
  area: '',
  address: '',
  avgPrice: 0,
  images: '',
  x: null,
  y: null,
  sold: 0,
  comments: 0,
  score: 50,       // 实际存储的得分值 (10-50)
  displayScore: 5   // 显示给用户的得分值 (1-5)
})

// 表单验证规则
const shopRules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  typeId: [{ required: true, message: '请选择店铺类型', trigger: 'change' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  x: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  y: [{ required: true, message: '请输入纬度', trigger: 'blur' }]
}

// 获取店铺列表
const getList = async () => {
  listLoading.value = true
  try {
    const res = await getShopList(listQuery)
    list.value = res.data.records.map(shop => ({
      ...shop,
      typeName: shopTypes.value.find(type => type.id === shop.typeId)?.name || '未知',
      displayScore: shop.score / 10 // 将数据库中的评分转换为1-5分制显示
    }))
    total.value = res.data.total
  } catch (error) {
    console.error('获取店铺列表失败', error)
  } finally {
    listLoading.value = false
  }
}

// 获取店铺类型列表
const getTypeList = async () => {
  try {
    const res = await getShopTypeList()
    shopTypes.value = res.data
  } catch (error) {
    console.error('获取店铺类型失败', error)
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

// 新增店铺
const handleAdd = () => {
  dialogType.value = 'add'
  Object.keys(shopForm).forEach(key => {
    if (key === 'avgPrice' || key === 'sold' || key === 'comments') {
      shopForm[key] = 0;
    } else if (key === 'score') {
      shopForm[key] = 50; // 默认5分 (5 * 10)
    } else if (key === 'displayScore') {
      shopForm[key] = 5; // 显示5分
    } else {
      shopForm[key] = '';
    }
  })
  dialogVisible.value = true
}

// 编辑店铺
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  try {
    const res = await getShopDetail(row.id)
    const shopData = res.data;
    // 将数据库中的score转换为前端显示的评分(1-5分)
    shopData.displayScore = shopData.score / 10;
    Object.assign(shopForm, shopData)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取店铺详情失败', error)
  }
}

// 删除店铺
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除店铺 ${row.name} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteShop(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除店铺失败', error)
    }
  }).catch(() => {})
}

// 提交表单
const submitShopForm = async () => {
  if (!shopFormRef.value) return
  
  try {
    await shopFormRef.value.validate()
    
    // 删除displayScore字段，服务器端不需要
    const shopData = { ...shopForm };
    delete shopData.displayScore;
    
    if (dialogType.value === 'add') {
      await addShop(shopData)
      ElMessage.success('添加成功')
    } else {
      await updateShop(shopData)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交表单失败', error)
  }
}

// 评分转换 (界面显示1-5分，数据库存储10-50)
const handleScoreChange = (value) => {
  shopForm.score = value * 10;
}

onMounted(() => {
  getTypeList()
  getList()
})
</script>

<style scoped>
.shop-container {
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