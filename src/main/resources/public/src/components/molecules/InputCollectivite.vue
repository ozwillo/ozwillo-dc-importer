<template>
	<div id="collectivite">
		<label v-bind:class="labelClass">Collectivité :
			<Input v-bind:idName="inputId" v-bind:class="inputClass" type="text" v-model="siret"/>
			<input type="text" enabled="false" v-model="nomOrganisation">
		</label>
	</div>
</template>

<script>

	import Input from '../atoms/Input'

	export default {
		name: "InputCollectivite",
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
    				siret: "Entrer siret collectivité",
    			}
    },
		computed: {
		  nomOrganisation: function () {
		    return this.siret + " " + "NOM COLLECTIVITE"  //TODO: calling DC to get organization name for given siret (axios)
		  }
		}
	}
</script>

<style scoped>
  #collectivite{
      padding-left: 10px;
      margin-top: 10px;
      margin-bottom: 10px;
      margin-left: 20%;
      margin-right: 20%;
      width: auto;
      height: auto;
    }
</style>
