<template>
	<Section className="demande-acces">
		<SideBar childId="demande-acces-sidebar" childClass="demande-acces-sidebar" v-bind:items="items"></SideBar>
		<h2>Formulaire de demande d'accès aux données d'un modèle</h2>
		<Demandeur></Demandeur>
	</Section>
</template>

<script>

	import Section from '../atoms/Section'
	import SideBar from '../molecules/SideBar'
	import Demandeur from '../organisms/Demandeur'

    export default {
        name: "DemandeAcces",
        components: {
        	Section,
        	SideBar,
        	Demandeur
        },
        data () {
        	return {
        		items: [
        			{ id: 0, text: 'Créer une demande d\'accès', link: '/create' },
        			{ id: 1, text: 'Charger une demande d\'accès pré-définie', link: '/load' } //TODO: give true link
        		]
        	}
        }
    }

</script>

<style scoped>

h2 {
  margin-top: 10px;
  margin-left: 200px;
  padding-left: 10px;
}

</style>
