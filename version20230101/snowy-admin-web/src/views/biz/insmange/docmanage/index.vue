<template>
<!--Full document manage文档管理-->
	<a-row>
		<a-col :span="5">
			<a-card class="cardImp" :bordered="false" :loading="cardLoading">
				<a-tree
					v-if="treeData.length > 0"
					v-model:expandedKeys="defaultExpandedKeys"
					:tree-data="treeData"
					:field-names="treeFieldNames"
					@select="treeSelect"
				>
				</a-tree>
				<a-empty v-else :image="Empty.PRESENTED_IMAGE_SIMPLE" />
			</a-card>
		</a-col>
		<a-col :span="19">
			<a-card :bordered="false">
				<s-table
					@resizeColumn="
						(w, col) => {
							col.width = w
						}
					"
					ref="table"
					:scroll="{ x: 200 }"
					:columns="columns"
					:data="loadDate1"
					:expand-row-by-click="true"
					bordered
					:alert="options.alert.show"
					:row-key="(record) => record.id"
					:row-selection="options.rowSelection"
				>
					<template #operator class="table-operator">
						<a-form
							ref="searchFormRef"
							name="advanced_search"
							class="ant-advanced-search-form"
							:model="searchFormState"
						>
							<a-row :gutter="24">
								<a-col :span="6">
									<a-form-item name="searchKey">
										<a-input v-model:value="searchFormState.searchKey" placeholder="请输入排查项目"></a-input>
									</a-form-item>
								</a-col>
								<a-col :span="6">
<!--									<a-form-item name="userStatus">
										<a-select v-model:value="searchFormState.userStatus" placeholder="请选择级别">
											<a-select-option v-for="item in statusData" :key="item.dictValue" :value="item.dictValue">{{
												item.name
											}}</a-select-option>
										</a-select>
									</a-form-item>-->
									<a-form-item  name="troubles_level">
										<a-select
											v-model:value="searchFormState.troubles_level"
											placeholder="请选择隐患级别"
											:options="troubles_levelOptions"
										>
										</a-select>
									</a-form-item>
								</a-col>
								<a-col :span="6">
									<a-button type="primary" @click="table.refresh(true)">{{ $t('common.searchButton') }}</a-button>
									<a-button class="snowy-buttom-left" @click="() => searchFormRef.resetFields()">{{
										$t('common.resetButton')
									}}</a-button>
								</a-col>
								<a-col :span="6">
									<a-button type="primary" class="primaryAdd" @click="form.onOpen()" v-if="hasPerm('add_danger')">
										<span>{{ $t('common.addButton') }}{{ $t('model.bizyinhuan') }}</span>
									</a-button>
									<a-button danger @click="removeBatchUser()" v-if="hasPerm('delete_danger')">{{
										$t('common.batchRemoveButton')
									}}</a-button>
								</a-col>
							</a-row>
						</a-form>
					</template>
					<template #bodyCell="{ column, record }">
						<!-- <template v-if="column.dataIndex === 'avatar'">
							<a-avatar :src="record.avatar" style="margin-bottom: -5px; margin-top: -5px" />
						</template> -->
						<template v-if="column.dataIndex === 'troubles_image'">
							<a-image :width="50" :src="record.troubles_image" />
						</template>
						<template v-if="column.dataIndex === 'update_image'">
							<a-image :width="50" :src="record.update_image" />
						</template>

						<!-- <template v-if="column.dataIndex === 'gender'">
							{{ $TOOL.dictTypeData('GENDER', record.gender) }}
						</template>
						<template v-if="column.dataIndex === 'userStatus'">
							<a-switch
								:loading="loading"
								:checked="record.userStatus === 'ENABLE'"
								@change="editStatus(record)"
								v-if="hasPerm('bizUserUpdataStatus')"
							/>
							<span v-else>{{ $TOOL.dictTypeData('COMMON_STATUS', record.userStatus) }}</span>
						</template> -->
						<template v-if="column.dataIndex === 'action'">
							<a @click="form.onOpen(record)" v-if="hasPerm('edit_danger')">{{ $t('common.editButton') }}</a>
							<!-- <a-divider type="vertical"  />
							<a @click="download(record)" >下载隐患图片</a>
							<a-divider type="vertical"  />
							<a @click="download(record)" >下载整改图片</a> -->
							<!-- <a-divider type="vertical" v-if="hasPerm(['bizUserEdit', 'bizUserGrantRole'], 'and')" /> -->
							<!-- <a @click="selectRole(record)" v-if="hasPerm('bizUserGrantRole')">角色</a>
							<a-divider type="vertical" v-if="hasPerm(['bizUserGrantRole', 'bizUserPwdReset'], 'and')" />
							<a-popconfirm title="确定重置此人员密码？" @confirm="resetPassword(record)">
								<a v-if="hasPerm('bizUserPwdReset')">重置密码</a>
							</a-popconfirm>
							<a-divider type="vertical" v-if="hasPerm(['bizUserPwdReset', 'bizUserDelete'], 'and')" /> -->
							<a-popconfirm title="确定要删除此条记录吗？" @confirm="removeUser(record)">
								<a-button type="link" danger size="small" v-if="hasPerm('delete_danger')">{{
									$t('common.removeButton')
								}}</a-button>
							</a-popconfirm>
						</template>
					</template>
				</s-table>
			</a-card>
		</a-col>
	</a-row>
	<Form ref="form" @successful="table.refresh(true)" />
	<role-selector-plus
		ref="RoleSelectorPlus"
		page-url="/biz/user/roleSelector"
		org-url="/biz/user/orgTreeSelector"
		:role-global="false"
		@onBack="roleBack"
	/>
</template>
<script setup name="yinhuanAPI">
	import { message, Empty } from 'ant-design-vue'
	import { getCurrentInstance } from 'vue'
	// import { getFileAccessHttpUrl } from '@/api/manage'
	import bizUserApi from '@/api/biz/bizUserApi'
	import yinhuanAPI from '@/api/biz/yinhuanAPI'
	import roleSelectorPlus from '@/components/Selector/roleSelectorPlus.vue'
	import Form from './form.vue'
	import fileApi from '@/api/dev/fileApi'

	const columns = [
		{
			title: '隐患级别',
			dataIndex: 'troubles_level',
			width: '80px',
			align: 'center',
			resizable:true
		},
		{
			title: '排查类型',
			dataIndex: 'troubleshoot_type',
			align: 'center',
			// width: '120px',
			resizable:true,
			width: '80px'
		},
		{
			title: '排查项目',
			dataIndex: 'troubleshoot_name',
			ellipsis: false,
			resizable: true,
			width: '80px'
		},

		{
			title: '隐患描述',
			dataIndex: 'troubles_detail',
			ellipsis: true,
			resizable: true,
			width: '80px'
		},
		{
			title: '隐患图片',
			dataIndex: 'troubles_image',
			resizable: true,
			width: '80px'
		},
		{
			title: '整改图片',
			dataIndex: 'update_image',
			resizable: true,
			width: '80px'
		},
		/*{
			title: '隐患级别',
			dataIndex: 'troubles_level',
			ellipsis: true
		},*/

		{
			title: '整改措施',
			dataIndex: 'deal_method',
			ellipsis: true,
			width: '80px'
		},
		{
			title: '责任人',
			dataIndex: 'troubleshoot_user',
			width: '80px'
		},
		{
			title: '排查时间',
			dataIndex: 'troubleshoot_time	',
			ellipsis: true,
			resizable: true,
			width: '80px'
		},
		{
			title: '完成时间',
			dataIndex: 'complete_time',
			width: '80px'
		}

		/*{
			title: '备注',
			dataIndex: 'others',
			width: '80px'
		}*/
	]
	if (hasPerm(['bizUserEdit', 'bizUserGrantRole', 'bizUserPwdReset', 'bizUserDelete'])) {
		columns.push({
			title: '操作',
			dataIndex: 'action',
			align: 'center',
			width: '60px',

		})
	}
	const { proxy } = getCurrentInstance()
	const statusData = proxy.$TOOL.dictTypeList('COMMON_STATUS')
	const searchFormRef = ref()
	let defaultExpandedKeys = ref([])
	let searchFormState = reactive({})

	const table = ref(null)
	const treeData = ref([])
	let selectedRowKeys = ref([])
	const treeFieldNames = { children: 'children', title: 'name', key: 'id' }
	let form = ref(null)

	let RoleSelector = ref()
	let RoleSelectorPlus = ref()
	const selectedRecord = ref({})
	const loading = ref(false)
	const cardLoading = ref(true)
	//隐患排查类型
	const troubles_levelOptions = proxy.$TOOL.dictTypeList('level_danger').map((item) => {
		return {
			value: item['dictValue'],
			label: item['name']
		}
	})
	// const slides = (record) => {

	// 	slides=yinhuanAPI.grantRole();
	// 	}
	// 表格查询 返回 Promise 对象

	const loadDate1 = (parameter) => {
		// fileApi.getImgFileToBase64(devFileIdParam)

		return yinhuanAPI.userPage(Object.assign(parameter, searchFormState)).then((res) => {
			return res
		})
	}
	const download = (record) => {
		if (record.troubles_image != null) {
			return fileApi.fileDownload(record.troubles_image)
		}
	}
	// const devFileIdParam = "127.0.0.1:82/dev/file/image"+"id=0000000"
	// const troubles_image = devFileIdParam

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
	// 点击树查询
	const treeSelect = (selectedKeys) => {
		if (selectedKeys.length > 0) {
			searchFormState.orgId = selectedKeys.toString()
		} else {
			delete searchFormState.orgId
		}
		table.value.refresh(true)
	}
	// 修改状态
	const editStatus = (record) => {
		loading.value = true
		if (record.userStatus === 'ENABLE') {
			bizUserApi.userDisableUser(record).then(() => {
				loading.value = false
				table.value.refresh()
			})
		} else {
			bizUserApi.userEnableUser(record).then(() => {
				loading.value = false
				table.value.refresh()
			})
		}
	}
	// 删除隐患
	const removeUser = (record) => {
		let params = [
			{
				id: record.id
			}
		]
		yinhuanAPI.safetyDelete(params).then(() => {
			table.value.refresh()
		})
	}
	// 批量删除隐患
	const removeBatchUser = () => {
		if (selectedRowKeys.value.length < 1) {
			message.warning('请选择一条或多条数据')
			return
		}
		const params = selectedRowKeys.value.map((m) => {
			return {
				id: m
			}
		})
		yinhuanAPI.safetyDelete(params).then(() => {
			table.value.clearRefreshSelected()
		})
	}

	// 打开角色选择器
	const selectRole = (record) => {
		selectedRecord.value = record
		// 查询到已有角色，并转为ids的格式，给角色选择器
		const param = {
			id: record.id
		}
		bizUserApi.userOwnRole(param).then((data) => {
			RoleSelectorPlus.value.showRolePlusModal(data)
		})
	}
	// 角色选择回调
	const roleBack = (value) => {
		let params = {
			id: selectedRecord.value.id,
			roleIdList: []
		}
		if (value.length > 0) {
			value.forEach((item) => {
				params.roleIdList.push(item.id)
			})
		}
		bizUserApi.grantRole(params).then(() => {})
	}
	//重置人员密码
	//
</script>

<style scoped>
	.record-img {
		width: 60px;
		height: 30px;
	}
	.cardImp {
		margin-right: 10px;
	}
	.ant-form-item {
		margin-bottom: 0 !important;
	}
	.primaryAdd {
		margin-right: 10px;
	}
	.snowy-table-avatar {
		margin-top: -10px;
		margin-bottom: -10px;
	}
	.snowy-buttom-left {
		margin-left: 8px;
	}
</style>
