<template>
	<section v-bind:class="className">
    <slot></slot>
	</section>
</template>

<script>
	export default {
		name: "Section",
		props: [
			'className'	//header, footer, body...
		]
	}
</script>

<style scoped>

</style>
