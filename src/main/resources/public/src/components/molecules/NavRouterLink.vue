<template>
	<router-link v-bind:to="linkedTo" v-bind:class="className" v-bind:routerLinkName="routerLinkName" v-bind:childId="childId" v-bind:childClass="childClass">
	  <slot></slot>
	  <Span v-bind:idName="childId" v-bind:className="childClass" v-bind:name="routerLinkName"></Span>
	</router-link>
</template>

<script>

	import Span from '../atoms/Span'

	export default {
		name: "Navbar-Router-Link",
		components: {
			Span
		},
		props: [
			'linkedTo',
			'className',
			'routerLinkName',
			'childId',
			'childClass'
		]
	}
</script>

<style scoped>

</style>
