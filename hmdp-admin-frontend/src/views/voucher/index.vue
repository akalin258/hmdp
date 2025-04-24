<template>
  <div class="voucher-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.title"
        placeholder="优惠券标题"
        class="filter-item"
        clearable
        @keyup.enter="handleFilter"
      />
      <el-select
        v-model="listQuery.type"
        placeholder="优惠券类型"
        clearable
        class="filter-item"
      >
        <el-option label="普通券" :value="0" />
        <el-option label="秒杀券" :value="1" />
      </el-select>
      <el-select
        v-model="listQuery.shopId"
        placeholder="选择店铺"
        clearable
        class="filter-item"
      >
        <el-option
          v-for="item in shopList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-button type="primary" @click="handleFilter">查询</el-button>
      <el-button type="success" @click="handleAdd(0)">新增普通券</el-button>
      <el-button type="warning" @click="handleAdd(1)">新增秒杀券</el-button>
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
      <el-table-column prop="shopName" label="店铺名称" width="150" />
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="subTitle" label="副标题" min-width="150" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type ? 'danger' : ''">
            {{ row.type ? '秒杀券' : '普通券' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rules" label="使用规则" min-width="200" />
      <el-table-column prop="actualValue" label="抵扣金额" width="100">
        <template #default="{ row }">
          ¥{{ (row.actualValue / 100).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="支付金额" width="100">
        <template #default="{ row }">
          ¥{{ (row.payValue / 100).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="使用条件" width="120">
        <template #default="{ row }">
          <span v-if="row.payValue > 0">满¥{{ (row.payValue / 100).toFixed(2) }}可用</span>
          <span v-else>无门槛</span>
        </template>
      </el-table-column>
      <el-table-column label="秒杀信息" width="200">
        <template #default="{ row }">
          <div v-if="row.type === 1">
            <div>开始: {{ formatDateTime(row.beginTime) }}</div>
            <div>结束: {{ formatDateTime(row.endTime) }}</div>
            <div>库存: {{ row.stock }}</div>
          </div>
          <div v-else>-</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
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

    <!-- 优惠券编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? (voucherForm.type === 0 ? '新增普通券' : '新增秒杀券') : '编辑优惠券'"
      width="600px"
    >
      <el-form
        ref="voucherFormRef"
        :model="voucherForm"
        :rules="voucherRules"
        label-width="100px"
      >
        <el-form-item label="店铺" prop="shopId">
          <el-select v-model="voucherForm.shopId" placeholder="请选择店铺">
            <el-option
              v-for="item in shopList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="voucherForm.title" />
        </el-form-item>
        <el-form-item label="副标题" prop="subTitle">
          <el-input v-model="voucherForm.subTitle" />
        </el-form-item>
        <el-form-item label="抵扣金额" prop="actualValue">
          <div class="input-with-unit">
            <el-input-number v-model="voucherForm.actualValue" :min="1" :precision="2" :step="0.01" />
            <span class="unit-text">元</span>
          </div>
          <span class="tip-text">用户可抵扣的金额</span>
        </el-form-item>
        <el-form-item label="支付金额" prop="payValue">
          <div class="input-with-unit">
            <el-input-number v-model="voucherForm.payValue" :min="0" :precision="2" :step="0.01" />
            <span class="unit-text">元</span>
          </div>
          <span class="tip-text">领取券需支付的金额，0表示免费券</span>
        </el-form-item>
        <el-form-item label="使用规则" prop="rules">
          <el-input v-model="voucherForm.rules" type="textarea" />
        </el-form-item>

        <!-- 秒杀券特有字段 -->
        <template v-if="voucherForm.type === 1">
          <el-form-item label="开始时间" prop="beginTime">
            <el-date-picker
              v-model="voucherForm.beginTime"
              type="datetime"
              placeholder="选择开始时间"
            />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="voucherForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
            />
          </el-form-item>
          <el-form-item label="库存" prop="stock">
            <el-input-number v-model="voucherForm.stock" :min="1" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitVoucherForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVoucherList, getVoucherDetail, addVoucher, addSeckillVoucher, updateVoucher, deleteVoucher } from '@/api/voucher'
import { getShopList } from '@/api/shop'

// 优惠券列表数据
const list = ref([])
const total = ref(0)
const listLoading = ref(false)
const shopList = ref([])

// 查询参数
const listQuery = reactive({
  current: 1,
  size: 10,
  title: '',
  type: '',
  shopId: ''
})

// 编辑表单数据
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const voucherFormRef = ref(null)
const voucherForm = reactive({
  id: undefined,
  shopId: '',
  title: '',
  subTitle: '',
  type: 0, // 0-普通券, 1-秒杀券
  actualValue: 1, // 修改为actualValue，表示抵扣金额
  payValue: 0, // 表示支付金额
  rules: '',
  beginTime: '',
  endTime: '',
  stock: 100
})

// 表单验证规则
const voucherRules = {
  shopId: [{ required: true, message: '请选择店铺', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  actualValue: [{ required: true, message: '请输入抵扣金额', trigger: 'blur' }], // 修改为actualValue
  beginTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取优惠券列表
const getList = async () => {
  listLoading.value = true
  try {
    const res = await getVoucherList(listQuery)
    // 处理返回数据，确保有正确的字段
    list.value = res.data.records.map(item => {
      // 如果后端返回value而不是actualValue，需要处理
      if (item.actualValue === undefined && item.value !== undefined) {
        item.actualValue = item.value;
      }
      return item;
    });
    total.value = res.data.total
  } catch (error) {
    console.error('获取优惠券列表失败', error)
  } finally {
    listLoading.value = false
  }
}

// 获取店铺列表
const getShops = async () => {
  try {
    const res = await getShopList({ size: 100 })
    shopList.value = res.data.records
  } catch (error) {
    console.error('获取店铺列表失败', error)
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

// 新增优惠券
const handleAdd = (type) => {
  dialogType.value = 'add'
  Object.keys(voucherForm).forEach(key => {
    if (key === 'actualValue') voucherForm[key] = 1 // 默认1元
    else if (key === 'payValue') voucherForm[key] = 0 // 默认0元
    else if (key === 'stock') voucherForm[key] = 100
    else if (key === 'type') voucherForm[key] = type
    else voucherForm[key] = ''
  })
  dialogVisible.value = true
}

// 编辑优惠券
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  try {
    const res = await getVoucherDetail(row.id)
    // 处理返回数据，确保有正确的字段
    const voucherData = res.data;
    // 如果后端返回value而不是actualValue，需要处理
    if (voucherData.actualValue === undefined && voucherData.value !== undefined) {
      voucherData.actualValue = voucherData.value;
    }
    
    // 将分转换为元，方便前端展示和编辑
    voucherData.actualValue = voucherData.actualValue / 100;
    voucherData.payValue = voucherData.payValue / 100;
    
    Object.assign(voucherForm, voucherData)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取优惠券详情失败', error)
  }
}

// 删除优惠券
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除优惠券 ${row.title} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteVoucher(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除优惠券失败', error)
    }
  }).catch(() => {})
}

// 提交表单
const submitVoucherForm = async () => {
  if (!voucherFormRef.value) return
  
  try {
    await voucherFormRef.value.validate()
    
    // 确保提交的表单数据正确
    const submitData = { ...voucherForm };
    
    // 将元转换回分，用于后端存储
    submitData.actualValue = Math.round(submitData.actualValue * 100);
    submitData.payValue = Math.round(submitData.payValue * 100);
    
    // 设置状态为上架(1)
    submitData.status = 1;
    
    // 删除不需要的字段，避免字段不一致
    if (submitData.value !== undefined && submitData.actualValue !== undefined) {
      delete submitData.value;
    }
    
    if (dialogType.value === 'add') {
      if (voucherForm.type === 0) {
        // 普通券
        await addVoucher(submitData)
      } else {
        // 秒杀券
        await addSeckillVoucher(submitData)
      }
      ElMessage.success('添加成功')
    } else {
      // 编辑
      await updateVoucher(submitData)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交表单失败', error)
  }
}

onMounted(() => {
  getShops()
  getList()
})
</script>

<style scoped>
.voucher-container {
  padding: 20px;
}

.filter-container {
  display: flex;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-item {
  width: 200px;
  margin-right: 10px;
  margin-bottom: 10px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.tip-text {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

.input-with-unit {
  display: flex;
  align-items: center;
}

.unit-text {
  margin-left: 8px;
  font-size: 14px;
}
</style> 