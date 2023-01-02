<template>
	<a-row>
		<!-- 左侧树 -->
		<a-col :span="5">
			<a-card class="cardImp" :bordered="false" :loading="cardLoading">
				<a-tree v-if="treeData.length > 0" v-model:expandedKeys="defaultExpandedKeys" :tree-data="treeData"
					:field-names="treeFieldNames" @select="treeSelect">
				</a-tree>
				<a-empty v-else :image="Empty.PRESENTED_IMAGE_SIMPLE" />
			</a-card>
		</a-col>
		<!-- 表格 -->
		<a-col :span="19">
			<!-- 整体界面分为两个卡片，此为上方第一个卡片 -->
			<a-card :bordered="false" style="margin-bottom: 10px">
				<a-form ref="searchFormRef" name="advanced_search" class="ant-advanced-search-form"
					:model="searchFormState">
					<a-row :gutter="24">
						<a-col :span="8">
							<a-form-item name="searchKey" label="名称关键词">
								<a-input v-model:value="searchFormState.searchKey" placeholder="请输入文件名称关键词"></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="8">
							<a-form-item name="engine" label="存储位置">
								<a-select v-model:value="searchFormState.engine" placeholder="请选择存储位置"
									:options="engineOptions">
								</a-select>
							</a-form-item>
						</a-col>
						<a-col :span="8">
							<a-button type="primary" @click="table.refresh(true)">
								<template #icon>
									<SearchOutlined />
								</template>
								查询
							</a-button>
							<a-button class="snowy-buttom-left" @click="() => searchFormRef.resetFields()">
								<template #icon>
									<redo-outlined />
								</template>
								重置
							</a-button>
						</a-col>
					</a-row>
				</a-form>
			</a-card>
			<a-card :bordered="false">
				<s-table ref="table" :columns="columns" :data="loadDate" :expand-row-by-click="true"
					:alert="options.alert.show" bordered :row-key="(record) => record.id"
					:row-selection="options.rowSelection">
					<template #operator class="table-operator">
						<a-space>
							<a-button type="primary" @click="() => uploadFormRef.openUpload()">
								<UploadOutlined />
								文件上传
							</a-button>
							<a-button danger @click="deleteBatchFile()">删除</a-button>
						</a-space>
					</template>
					<template #bodyCell="{ column, record }">
						<template v-if="column.dataIndex === 'thumbnail'">
							<img :src="record.thumbnail" class="record-img" v-if="
								record.suffix === 'png' ||
								record.suffix === 'jpg' ||
								record.suffix === 'jpng' ||
								record.suffix === 'ico' ||
								record.suffix === 'gif'
							" />
							<img src="/src/assets/images/fileImg/docx.png" class="record-img"
								v-else-if="record.suffix === 'doc' || record.suffix === 'docx'" />
							<img src="/src/assets/images/fileImg/xlsx.png" class="record-img"
								v-else-if="record.suffix === 'xls' || record.suffix === 'xlsx'" />
							<img src="/src/assets/images/fileImg/zip.png" class="record-img"
								v-else-if="record.suffix === 'zip'" />
							<img src="/src/assets/images/fileImg/rar.png" class="record-img"
								v-else-if="record.suffix === 'rar'" />
							<img src="/src/assets/images/fileImg/ppt.png" class="record-img"
								v-else-if="record.suffix === 'ppt' || record.suffix === 'pptx'" />
							<img src="/src/assets/images/fileImg/txt.png" class="record-img"
								v-else-if="record.suffix === 'txt'" />
							<img src="/src/assets/images/fileImg/html.png" class="record-img"
								v-else-if="record.suffix === 'html'" />
							<img src="/src/assets/images/fileImg/file.png" class="record-img" v-else />
						</template>
						<template v-if="column.dataIndex === 'engine'">
							{{ $TOOL.dictTypeData('FILE_ENGINE', record.engine) }}
						</template>
						<template v-if="column.dataIndex === 'action'">
							<a @click="detailRef.onOpen(record)">详情</a>
							<a-divider type="vertical" />
							<a :href="record.downloadPath" target="_blank">下载</a>
							<a-divider type="vertical" />
							<a-popconfirm title="删除此文件？" @confirm="deleteFile(record)">
								<a-button type="link" danger size="small">删除</a-button>
							</a-popconfirm>
						</template>
					</template>
				</s-table>
			</a-card>
			<uploadForm ref="uploadFormRef" @successful="table.refresh(true)" />
			<detail ref="detailRef" />
		</a-col>
	</a-row>
	<Form ref="form" @successful="table.refresh(true)"/>
	<role-selector-plus
		ref="RoleSelectorPlus"
		page-url="/biz/user/roleSelector"
		org-url="/biz/user/orgTreeSelector"
		:role-global="false"
		@onBack="roleBack"
	/>
</template>

<script setup name="devFile">
import { message ,Empty} from 'ant-design-vue'
import fileApi from '@/api/dev/fileApi'
import uploadForm from './uploadForm.vue'
import detail from './detail.vue'
import tool from '@/utils/tool'

import { getCurrentInstance } from 'vue'
import bizUserApi from '@/api/biz/bizUserApi'
import roleSelectorPlus from '@/components/Selector/roleSelectorPlus.vue'

// 定义tableDOM
const uploadFormRef = ref()
const detailRef = ref()
const { proxy } = getCurrentInstance()
const statusData = proxy.$TOOL.dictTypeList('COMMON_STATUS')
let defaultExpandedKeys = ref([])
const treeData = ref([])
let RoleSelector = ref()
let RoleSelectorPlus = ref()
const selectedRecord = ref({})
const loading = ref(false)
const cardLoading = ref(true)



const searchFormRef = ref()
let searchFormState = reactive({})
const table = ref(null)
const treeFieldNames = { children: 'children', title: 'name', key: 'id' }
let form = ref(null)

const columns = [
	{
		title: '文件名称',
		dataIndex: 'name',
		ellipsis: true,
		width: '280px'
	},
	{
		title: '缩略图',
		dataIndex: 'thumbnail',
		ellipsis: true,
		width: '80px'
	},
	{
		title: '文件大小',
		dataIndex: 'sizeInfo',
		ellipsis: true
	},
	{
		title: '文件后缀',
		dataIndex: 'suffix',
		ellipsis: true
	},
	{
		title: '储存引擎',
		dataIndex: 'engine',
		ellipsis: true
	},
	{
		title: '操作',
		dataIndex: 'action',
		align: 'center',
		width: '180px'
	}
]
let selectedRowKeys = ref([])
// 列表选择配置
const options = {
	alert: {
		show: false,
		clear: () => {
			selectedRowKeys = ref([])
		}
	},
	rowSelection: {
		onChange: (selectedRowKey, selectedRows) => {
			selectedRowKeys.value = selectedRowKey
		}
	}
}
// 表格查询 返回 Promise 对象
// const loadDate = (parameter) => {
// 	return fileApi.filePage(Object.assign(parameter, searchFormState)).then((data) => {
// 		return data
// 	})
// }

// 文件上传表格
const loadDate = (parameter) => {
	return fileApi.fileProgramme(Object.assign(parameter, searchFormState)).then((data) => {
		return data
	})
}

// 新增

// 左侧树查询
bizUserApi.userOrgTreeSelector().then((res) => {
		cardLoading.value = false
		if (res !== null) {
			treeData.value = res
			// 默认展开2级
			treeData.value.forEach((item) => {
				// 因为0的顶级
				if (item.parentId === '0') {
					defaultExpandedKeys.value.push(item.id)
					// 取到下级ID
					if (item.children) {
						item.children.forEach((items) => {
							defaultExpandedKeys.value.push(items.id)
						})
					}
				}
			})
		}
	})
	// 点击树查询
	const treeSelect = (selectedKeys) => {
		if (selectedKeys.length > 0) {
			searchFormState.orgId = selectedKeys.toString()
		} else {
			delete searchFormState.orgId
		}
		console.log(searchFormState.orgId)
		table.value.refresh(true)
	}

// 删除
const deleteFile = (record) => {
	let params = [
		{
			id: record.id
		}
	]
	fileApi.fileDelete(params).then(() => {
		table.value.refresh(true)
	})
}
// 批量删除
const deleteBatchFile = () => {
	if (selectedRowKeys.value.length < 1) {
		message.warning('请选择一条或多条数据')
		return false
	}
	const params = selectedRowKeys.value.map((m) => {
		return {
			id: m
		}
	})
	fileApi.fileDelete(params).then(() => {
		table.value.clearRefreshSelected()
	})
}
// 存储位置
const engineOptions = tool.dictTypeList('FILE_ENGINE').map((item) => {
	return {
		value: item['dictValue'],
		label: item['name']
	}
})
</script>

<style scoped>

.cardImp {
		margin-right: 10px;
	}
.record-img {
	width: 40px;
	height: 40px;
}

.ant-form-item {
	margin-bottom: 0 !important;
}

.snowy-buttom-left {
	margin-left: 8px;
}
</style>
