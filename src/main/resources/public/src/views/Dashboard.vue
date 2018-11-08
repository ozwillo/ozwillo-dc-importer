<template>
    <table>
        <tr>
            <td>
                <div>
                    There is {{ requestToValid }} data access request needing validation
                </div>
                <router-link to="/tovalidate">[Insertion d'un lien vers la liste de formulaire à valider pour la collectivité ici]</router-link>
            </td>
        </tr>
    </table>
</template>

<script>
    import axios from 'axios'
    export default {
        name: "Dashboard",
        data () {
            return {
                requestToValid: 0,
                errors: []
            }
        },
        beforeCreate() {
            axios.get('api/data_access_request/123456789?state=sent')
            .then(response => {
                this.requestToValid = response.data.length
            })
            .catch(e => {
                this.errors.push(e)
            })            
        }
    }
</script>

<style scoped>
</style>
