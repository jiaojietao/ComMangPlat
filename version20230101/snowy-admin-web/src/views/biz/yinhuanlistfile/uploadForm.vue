<template>
	<a-drawer
		title="文件上传"
		:width="500"
		:visible="visible"
		:destroy-on-close="true"
		:footer-style="{ textAlign: 'right' }"
		:bodyStyle="{ 'padding-top': '0px' }"
		@close="onClose"
	>
		<a-tabs v-model:activeKey="activeKey">
			<a-tab-pane key="Local" tab="本地">
				<a-spin :spinning="uploadLoading">
					<a-upload-dragger :show-upload-list="false" :custom-request="customRequestLocal">
						<p class="ant-upload-drag-icon">
							<inbox-outlined></inbox-outlined>
						</p>
						<p class="ant-upload-text">单击或拖动文件到此区域进行上传</p>
						<p class="ant-upload-hint">支持单个上传</p>
					</a-upload-dragger>
				</a-spin>
			</a-tab-pane>
			<!-- <a-tab-pane key="Aliyun" tab="阿里云">
				<a-spin :spinning="uploadLoading">
					<a-upload-dragger :custom-request="customRequestAliyun" :show-upload-list="false">
						<p class="ant-upload-drag-icon">
							<inbox-outlined></inbox-outlined>
						</p>
						<p class="ant-upload-text">单击或拖动文件到此区域进行上传</p>
						<p class="ant-upload-hint">支持单个上传</p>
					</a-upload-dragger>
				</a-spin>
			</a-tab-pane>
			<a-tab-pane key="Tencent" tab="腾讯云">
				<a-spin :spinning="uploadLoading">
					<a-upload-dragger :custom-request="customRequestTencent" :show-upload-list="false">
						<p class="ant-upload-drag-icon">
							<inbox-outlined></inbox-outlined>
						</p>
						<p class="ant-upload-text">单击或拖动文件到此区域进行上传</p>
						<p class="ant-upload-hint">支持单个上传</p>
					</a-upload-dragger>
				</a-spin>
			</a-tab-pane>
			<a-tab-pane key="Minio" tab="MINIO">
				<a-spin :spinning="uploadLoading">
					<a-upload-dragger :custom-request="customRequestMinio" :show-upload-list="false">
						<p class="ant-upload-drag-icon">
							<inbox-outlined></inbox-outlined>
						</p>
						<p class="ant-upload-text">单击或拖动文件到此区域进行上传</p>
						<p class="ant-upload-hint">支持单个上传</p>
					</a-upload-dragger>
				</a-spin>
			</a-tab-pane> -->
		</a-tabs>
	</a-drawer>
</template>

<script setup name="uploadForm">
	import { message } from 'ant-design-vue'
	import fileApi from '@/api/dev/fileApi'
	// 定义emit事件
	const emit = defineEmits({ successful: null })
	// 默认是关闭状态
	let visible = $ref(false)
	const activeKey = ref('Local')
	const uploadLoading = ref(false)

	// 打开抽屉
	const openUpload = () => {
		visible = true
	}
	// 关闭抽屉
	const onClose = () => {
		visible = false
		emit('successful')
	}
	// 上传本地文件
	const customRequestLocal = (data) => {
		uploadLoading.value = true
		const fileData = new FormData()
		fileData.append('file', data.file)
		fileApi
			.fileUploadListReturnUrl(fileData)
			.then(() => {
				message.success('上传成功')
			})
			.finally(() => {
				uploadLoading.value = false
			})
	}
	
	// // 上传阿里云文件
	// const customRequestAliyun = (data) => {
	// 	uploadLoading.value = true
	// 	const fileData = new FormData()
	// 	fileData.append('file', data.file)
	// 	fileApi
	// 		.fileUploadAliyunReturnUrl(fileData)
	// 		.then(() => {
	// 			message.success('上传成功')
	// 		})
	// 		.finally(() => {
	// 			uploadLoading.value = false
	// 		})
	// }
	// // 上传腾讯文件
	// const customRequestTencent = (data) => {
	// 	uploadLoading.value = true
	// 	const fileData = new FormData()
	// 	fileData.append('file', data.file)
	// 	fileApi
	// 		.fileUploadTencentReturnUrl(fileData)
	// 		.then(() => {
	// 			message.success('上传成功')
	// 		})
	// 		.finally(() => {
	// 			uploadLoading.value = false
	// 		})
	// }
	// // 上传腾讯文件
	// const customRequestMinio = (data) => {
	// 	uploadLoading.value = true
	// 	const fileData = new FormData()
	// 	fileData.append('file', data.file)
	// 	fileApi
	// 		.fileUploadMinioReturnUrl(fileData)
	// 		.then(() => {
	// 			message.success('上传成功')
	// 		})
	// 		.finally(() => {
	// 			uploadLoading.value = false
	// 		})
	// }
	// 调用这个函数将子组件的一些数据和方法暴露出去
	defineExpose({
		openUpload
	})
</script>
