<template>
	<div id="mail">
		<label v-bind:class="labelClass">Mail :
			<Input v-bind:idName="inputId" v-bind:class="inputClass" type="text" v-model="mail"/>
		</label>
	</div>
</template>

<script>

	import Input from '../atoms/Input'

	export default {
		name: "InputMail",
		components: {
			Input
		},
		props: [
			'labelClass',
			'inputId',
			'inputClass'
		],
		data () {
    			return {
    				mail: "Entrer un mail"
    			}
    }
	}
</script>

<style scoped>
  #mail{
      padding-left: 10px;
      margin-top: 10px;
      margin-bottom: 10px;
      margin-left: 20%;
      margin-right: 20%;
      width: auto;
      height: auto;
    }

    .mail-input {
        margin-left: 47px;
      }

</style>
