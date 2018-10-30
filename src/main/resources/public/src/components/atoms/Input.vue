<template>
	<input v-bind:id="idName" v-bind:class="className" v-bind:type="type" v-bind:value="value" v-on:input="$emit('input', $event.target.value)"/>
</template>

<script>
	export default {
		name: "Input",
		model: {
		  prop: 'value',
		  event: 'input'
		},
		props: [
			'idName',
			'className',
			'type',
			'value'
		]
	}
</script>

<style scoped>

</style>
