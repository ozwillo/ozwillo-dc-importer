<template>
	<div v-bind:class="className">
	  <slot></slot>
	</div>
</template>

<script>
	export default {
		name: "Div",
		props: [
		  'className'
		]
	}
</script>

<style scoped>

</style>
