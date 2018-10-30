<template>
	<Div className="sidebar">
		<Div className="sidebar-inner">
			<List v-bind:listId="childId" v-bind:className="childClass" v-bind:items="items"></List>
		</Div>
	</Div>
</template>

<script>

	import Div from '../atoms/Div'
	import List from '../atoms/List'

	export default {
        name: "SideBar",
        components: {
        	Div,
        	List
        },
        props: [
        	'childId',
        	'childClass',
        	'items'
        ]
    }
</script>

<style scoped>
    .sidebar .sidebar-inner{
      width: 200px;
      padding: 10px 10px 10px 10px;
      }
  	.sidebar{
  		position: fixed;
  		top: 57px;
  		left: 0;
  		bottom: 0;
  		overflow-x: hidden;
  		overflow-y: auto;
  		background-color: grey;
  	}
</style>
