<template>
    <div class="container">
        <h2>Access validation</h2>
        <data-access-validation></data-access-validation>
        <router-link to="/">
            <input class="validation-buttons" type="button" @click="validAccess" value="Valid" v-bind:disabled="disabled">
        </router-link>
        <router-link to="/">
            <input class="validation-buttons" type="button" @click="refuseAccess" value="Refuse">
        </router-link>
    </div>
</template>

<script>
import DataAccessValidation from '../components/organisms/DataAccessValidation'
export default {
    name: 'Validation',
    components: {
        DataAccessValidation
    },
    data () {
        return {
            disabled: false
        }
    },
    methods: {
        validAccess: function(){
            // Here PUT API with id in request url parameter to update document state to VALIDATED
            console.log('state changed for \"VALIDATED\"')
        },
        refuseAccess: function(){
            // Here PUT API with id in request url parameter to update document state to REFUSED
            console.log('state changed for \"REFUSED\"')
        }
    }
}
</script>

<style scoped>
    .validation-buttons{
        margin-top: 50px;
        margin-right: 5px;
    }
</style>


