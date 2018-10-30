<template>
	<ul v-bind:class="className" v-bind:id="listId" v-bind:items="items">
		<li v-for="item in items" v-bind:item="item.text" v-bind:key="item.id">
			<a v-bind:href="item.link">{{ item.text }}</a>
		</li>
	</ul>
</template>

<script>

	export default {
        name: "List",
        props: [
          'className',
          'listId',
        	'item',
        	'items'
        ]
    }
</script>

<style scoped>

</style>
