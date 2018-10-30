<template>
	<div id="demandeur-acces">
	  <h3>Renseignements demandeur : </h3>
		<InputCollectivite labelClass="collectivite-label" inputId="collectivite-input" inputClass="collectivite-input"></InputCollectivite>
		<InputMail labelClass="mail-label" inputId="mail-input" inputClass="mail-input"></InputMail>
	</div>
</template>

<script>

	import InputCollectivite from '../molecules/InputCollectivite'
	import InputMail from '../molecules/InputMail'

	export default {
		name: "Demandeur",
		components: {
			InputCollectivite,
			InputMail
		}
	}
</script>

<style scoped>

  #demandeur-acces {
    margin-left: 210px;
    margin-right: 10px;
    margin-top: 20px;
    padding-left: 10px;
  }

  h3 {
    margin-left: 10%;
  }

</style>
