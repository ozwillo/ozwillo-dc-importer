<template>
	<img src="../../assets/logo-ozwillo.png"
		v-bind:width="width"
		v-bind:height="height"
		v-bind:class="className"
		v-bind:alt="altName" />
</template>

<script>
	export default {
		name: "Logo",
		props: [
			'width',
			'height',
			'className',
			'altName'
		]
	}
</script>

<style scoped>

</style>
