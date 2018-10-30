<template>
	<span v-bind:id="idName" v-bind:class="className" v-bind:name="name">{{ name }}</span>
</template>

<script>
	export default {
		name: "Span",
		props: [
			'idName',
			'className',
			'name'
		]
	}
</script>

<style scoped>
    #app-name {
        color: white
      }
</style>
