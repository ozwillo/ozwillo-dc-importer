<template>
	<button
		type="button"
		v-bind:class="className"
		v-bind:data-toggle="dataToggle"
		v-bind:data-target="dataTarget"
		v-bind:aria-controls="ariaControls"
		v-bind:aria-expanded="ariaExpanded"
		v-bind:aria-label="ariaLabel">
		<slot></slot>
	</button>
</template>

<script>
	export default {
		name: "Button",
		props: [
		'className',
		'dataToggle',
		'dataTarget',
		'ariaControls',
		'ariaExpanded',
		'ariaLabel'
		]
	}
</script>

<style scoped>

</style>
