<template>
	<a-drawer
		:title="formData.id ? '编辑隐患' : '增加隐患'"
		:width="620"
		:visible="visible"
		:destroy-on-close="true"
		:body-style="{ paddingBottom: '80px', 'padding-top': '0px' }"
		:footer-style="{ textAlign: 'right' }"
		@close="onClose"
	>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
			<a-tabs v-model:activeKey="activeTabsKey">
				<a-tab-pane key="1" tab="基础信息" force-render>
					<a-row :gutter="16">
						<a-col :span="12">
							<a-form-item label="排查类型：" name="troubleshoot_type">
								<a-select
									v-model:value="formData.troubleshoot_type"
									placeholder="请选择排查类型"
									:options="cultureLevelOptions"
								>
								</a-select>
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="排查项目：" name="troubleshoot_name">
								<a-input v-model:value="formData.troubleshoot_name" placeholder="请输入排查项目" allow-clear />
							</a-form-item>
						</a-col>
					</a-row>
					<a-row :gutter="16">
						<a-col :span="12">
							<a-form-item label="排查时间：" name="troubleshoot_time">
								<a-date-picker v-model:value="formData.troubleshoot_time" value-format="YYYY-MM-DD"> </a-date-picker>
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="隐患明细：" name="troubles_detail">
								<a-input v-model:value="formData.troubles_detail" placeholder="请输入隐患明细" allow-clear />
							</a-form-item>
						</a-col>
					</a-row>
					<a-row :gutter="16">
						<a-col :span="12">
							<template v-if="formData.troubles_image">
								<a-image :src="formData.troubles_image"></a-image>
							</template>

							<a-form-item label="隐患图片：" name="troubles_image">
								<a-input v-model:value="formData.troubles_image" allow-clear hidden />
								<a-upload :custom-request="customRequestLocal">
									<a-button> <a-icon type="upload" /> 点击上传隐患图片 </a-button>
								</a-upload>
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<template v-if="formData.update_image">
								<a-image :src="formData.update_image"></a-image>
							</template>
							<a-form-item label="整改图片：" name="update_image">
								<a-input v-model:value="formData.update_image" allow-clear hidden />
								<a-upload :custom-request="customRequestLocal_update">
									<a-button> <a-icon type="upload" /> 点击上传整改图片 </a-button>
								</a-upload>
							</a-form-item>
						</a-col>
					</a-row>
					<a-row :gutter="16">
						<a-col :span="12">
							<a-form-item label="隐患级别：" name="troubles_level">
								<a-select
									v-model:value="formData.troubles_level"
									placeholder="请选择排查类型"
									:options="troubles_levelOptions"
								>
								</a-select>
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="整改措施：" name="deal_method">
								<a-input v-model:value="formData.deal_method" placeholder="请输入整改措施" style="width: 100%" />
							</a-form-item>
						</a-col>
					</a-row>
					<a-row :gutter="16">
						<a-col :span="12">
							<a-form-item label="责任人：" name="troubleshoot_user">
								<a-input v-model:value="formData.troubleshoot_user" placeholder="请输入责任人" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="8">
							<a-form-item label="完成时间：" name="complete_time">
								<a-date-picker
									v-model:value="formData.complete_time"
									placeholder="完成时间"
									value-format="YYYY-MM-DD"
									allow-clear
								/>
							</a-form-item>
						</a-col>
					</a-row>
					<a-row :gutter="16">
						<a-col :span="24">
							<a-form-item label="备注：" name="others">
								<a-textarea v-model:value="formData.others" placeholder="备注" allow-clear />
							</a-form-item>
						</a-col>
					</a-row>
				</a-tab-pane>
			</a-tabs>
		</a-form>
		<template #footer>
			<a-button style="margin-right: 8px" @click="onClose">关闭</a-button>
			<a-button type="primary" :loading="formLoading" @click="onSubmit">保存</a-button>
		</template>
	</a-drawer>
</template>

<script setup>
	import yinhuanAPI from '@/api/biz/yinhuanAPI'
	import { required, rules } from '@/utils/formRules'

	import { message } from 'ant-design-vue'
	import fileApi from '@/api/dev/fileApi'

	// 默认是关闭状态
	let visible = $ref(false)
	const formRef = ref()
	const activeTabsKey = ref('1')
	const { proxy } = getCurrentInstance()
	const emit = defineEmits({ successful: null })
	const formLoading = ref(false)
	const treeData = ref([])
	const treeDefaultExpandedKeys = ref([])
	const uploadLoading = ref(false)

	// 主职职位数据
	let positionData = ref([])
	// 主职主管人员数据
	let directorData = ref([])

	// 定义一个装机构跟职位的壳
	let childrenOrgPosArray = ref([])
	// 表单数据
	let formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		visible = true
		formData.value = {
			gender: '男',
			positionJson: []
		}
		if (record) {
			convertFormData(record)
		}
		// 机构选择器数据
		yinhuanAPI.userOrgTreeSelector().then((res) => {
			if (res !== null) {
				treeData.value = res
				// 默认展开2级
				treeData.value.forEach((item) => {
					// 因为0的顶级
					if (item.parentId === '0') {
						treeDefaultExpandedKeys.value.push(item.id)
						// 取到下级ID
						if (item.children) {
							item.children.forEach((items) => {
								treeDefaultExpandedKeys.value.push(items.id)
							})
						}
					}
				})
			}
		})
	}
	// 关闭抽屉
	const onClose = () => {
		treeData.value = []
		treeDefaultExpandedKeys.value = []
		positionData.value = []
		directorData.value = []
		visible = false
	}
	// 回显数据
	const convertFormData = (record) => {
		const param = {
			id: record.id
		}
		// 查询详情
		yinhuanAPI.userDetail(param).then((data) => {
			if (data.positionJson) {
				const positionJsonLocal = JSON.parse(data.positionJson).map((item) => {
					childOrgSelect(item)
					return item
				})
				// 替换表单中的格式与后端查到的
				data.positionJson = positionJsonLocal
			}
			formData.value = Object.assign(formData.value, data)
			selePositionData(formData.value.orgId)
		})
	}
	// 默认要校验的
	const formRules = {
		account: [required('请输入账号')],
		name: [required('请输入姓名')],
		sex: [required('请选择性别')],
		orgId: [required('请选择组织')],
		positionId: [required('请选择岗位')]
	}
	// 机构选择后查询对应的职位
	const selePositionData = (orgId, type) => {
		if (orgId) {
			const param = {
				orgId: orgId
			}
			yinhuanAPI.userPositionSelector(param).then((res) => {
				positionData.value = res
			})
			yinhuanAPI.userSelector(param).then((res) => {
				directorData.value = res
			})
			// 此类型代表选择的时候重置后面的职位
			if (type === 0) {
				formData.value.positionId = undefined
				formData.value.directorId = undefined
			}
		} else {
			formData.value.positionId = undefined
			formData.value.directorId = undefined
		}
	}
	// 附属职位信息增行
	const addDomains = () => {
		if (formData.value.positionJson === null) {
			formData.value.positionJson = []
		}
		formData.value.positionJson.push({
			orgId: undefined,
			positionId: undefined,
			directorId: undefined
		})
	}
	// 删减行
	const delDomains = (index) => {
		formData.value.positionJson.splice(index, 1)
	}
	// 子表行内选择机构
	const childOrgSelect = async (data, type) => {
		// 说明正在切换机构，我们就将他的后面的设置空
		if (type === 0) {
			formData.value.positionJson.filter((item) => {
				if (item.orgId === data.orgId) {
					item.positionId = undefined
					item.directorId = undefined
				}
			})
		}
		const param = {
			orgId: data.orgId
		}
		// 查询职位
		const posList = await yinhuanAPI.userPositionSelector(param)
		// 查询人员
		const userList = await yinhuanAPI.userSelector(param)
		const obj = {
			orgId: data.orgId,
			posList: posList,
			userList: userList
		}
		childrenOrgPosArray.value.push(obj)
	}
	// 获取行内职位数据
	const childPosData = (value) => {
		const resultData = childrenOrgPosArray.value.filter((item) => item.orgId === value)
		if (resultData.length > 0) {
			return resultData[0].posList
		}
	}
	// 获取行内人员数据
	const childUserData = (value) => {
		const resultData = childrenOrgPosArray.value.filter((item) => item.orgId === value)
		if (resultData.length > 0) {
			return resultData[0].userList
		}
	}
	// 验证并提交数据
	const onSubmit = () => {
		formRef.value.validate().then(() => {
			// 因为不切断，我下面转换数据格式，影响上面表单会报错
			let formDatas = JSON.parse(JSON.stringify(formData.value))
			formDatas.positionJson = JSON.stringify(formDatas.positionJson)
			formLoading.value = true
			yinhuanAPI
				.submitForm(formDatas, !formDatas.id)
				.then(() => {
					onClose()
					emit('successful')
				})
				.finally(() => {
					formLoading.value = false
				})
		})
	}

	//隐患排查类型
	const cultureLevelOptions = proxy.$TOOL.dictTypeList('total_danger').map((item) => {
		return {
			value: item['dictValue'],
			label: item['name']
		}
	})
	//隐患排查类型
	const troubles_levelOptions = proxy.$TOOL.dictTypeList('level_danger').map((item) => {
		return {
			value: item['dictValue'],
			label: item['name']
		}
	})
	const customRequestLocal = (data) => {
		uploadLoading.value = true
		const fileData = new FormData()
		fileData.append('file', data.file)
		fileApi
			.fileUploadYinhuanReturnUrl(fileData)
			.then((res) => {
				let path = res.split('=')[1]
				console.log(res)
				;(formData.value.troubles_image = path), message.success('上传成功')
			})
			.finally(() => {
				uploadLoading.value = false
			})
	}
	const customRequestLocal_update = (data) => {
		uploadLoading.value = true
		const fileData = new FormData()
		fileData.append('file', data.file)
		fileApi
			.fileUploadYinhuanReturnUrl(fileData)
			.then((res) => {
				let path = res.split('=')[1]
				// console.log(path)
				;(formData.value.update_image = path), message.success('上传成功')
			})
			.finally(() => {
				uploadLoading.value = false
			})
	}
	// 调用这个函数将子组件的一些数据和方法暴露出去
	defineExpose({
		onOpen
	})
</script>

<style scoped type="less">
	.childAddButton {
		margin-bottom: 10px;
	}
	.form-row {
		background-color: var(--item-hover-bg);
		margin-left: 0px !important;
	}
	.form-row-con {
		padding-bottom: 5px;
		padding-top: 5px;
		padding-left: 15px;
	}
	.dashedButton {
		margin-top: 10px;
		width: 100%;
	}
	.form-div {
		padding-top: 10px;
	}
</style>
