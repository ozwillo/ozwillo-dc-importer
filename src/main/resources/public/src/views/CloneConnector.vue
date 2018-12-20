<template>
    <div class="container">
        <h2>Clone selected connector</h2>
        <form>
            <div class="form-group row">
                <label for="application-name-input" class="col-sm-3 col-form-label col-form-label-sm">Application: </label>
                <input type="text" id="application-name-input" v-model="selectedConnector.applicationName" disabled/>
            </div>
            <div class="form-group row">
                <label for="new-organization-claimer" class="col-sm-3 col-form-label col-form-label-sm">
                    Collectivité: 
                </label>
                <vue-bootstrap-typeahead
                    id="new-organization-claimer"
                    ref="neworganizationclaimer"
                    placeholder="Find an Organization"
                    :minMatchingChars="0"
                    v-model="organizationSearch"
                    :data="organizations"
                    :serializer="displayingResultOfOrganizationSearch"
                    :append="organizationSelected.siret"
                    @hit="organizationSelected = $event"    
                />
            </div>
            <div class="form-group row">
                <label for="login-claimer" class="col-sm-3 col-form-label col-form-label-sm">
                    Login
                </label>
                <input type="text" id="login-claimer" v-model="selectedConnector.login"/>
            </div>
            <div :class="checkPassword">
                <div class="form-group row">
                    <label for="password-claimer" class="col-sm-3 col-form-label col-form-label-sm">
                        Password
                    </label>
                    <input type="password" id="password-claimer" v-model="selectedConnector.password" placeholder="Type password"/>
                </div>
            </div>
            <div class="form-group row">
                <div class="col-sm-3"></div>
                <input type="password" id="password-control-claimer" v-model="controlPassword" placeholder="Confirm password"/>
            </div>
            <div class="form-group row">
                <label for="token-claimer" class="col-sm-3 col-form-label col-form-label-sm">
                    Secret or Token
                </label>
                <input type="password" id="token-claimer" v-model="selectedConnector.secretOrToken"/>
            </div>
            <div class="form-group row">
                <label for="instance-id-claimer" class="col-sm-3 col-form-label col-form-label-sm">
                    Instance Id
                </label>
                <input type="text" id="instance-id-claimer" v-model="selectedConnector.instanceId"/>
            </div>
            <div class="for-group row">
                <input type="button" value="Create" :disabled="disabled" @click="cloneConnector"/>
                <input type="button" value="Cancel" @click="backToConnectorsManagament"/>
            </div>
        </form>
    </div>
</template>

<script>

import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
import axios from 'axios'
import debounce from 'lodash/debounce'

export default {
    name: 'cloneConnector',
    components: {
        VueBootstrapTypeahead
    },
    data() {
        return {
            selectedConnector: {
                id: '',
                applicationName: '',
                baseUrl: '',
                organizationSiret: ''
            },
            organizations: [],
            organizationSearch: '',
            organizationSelected: {},
            controlPassword: '',
            isOk: true,
            isNok: false,
            errors: []
        }
    },
    watch: {
        organizationSearch: debounce(function(name) { this.getOrganizations(name) }, 500),
        organizationSelected: {
            handler (val, oldVal){
                this.selectedConnector.organizationSiret = val.siret
                this.$refs.neworganizationclaimer.$data.inputValue = val.denominationUniteLegale
            },
            deep: true
        },
        selectedConnector: {
            handler (val, oldVal){
                if (val.password !== this.controlPassword){
                    this.isOk = false
                    this.isNok = true
                }else{
                    this.isOk = true
                    this.isNok = false
                }
            },
            deep: true
        },
        controlPassword (val, oldVal){
            if (val !== this.selectedConnector.password){
                this.isOk = false
                this.isNok = true
            }else{
                this.isOk = true
                this.isNok = false
            }
        }
    },
    computed: {
        disabled (){
            return (Object.keys(this.organizationSelected).length === 0 || this.controlPassword !== this.selectedConnector.password)
        },
        checkPassword (){
            return {
                passOk: this.isOk && !this.isNok,
                passNok: !this.isOk && this.isNok
            }
        }
    },
    created (){
        this.selectedConnector = {
            id: this.$route.params.id,
            applicationName: this.$route.params.appName,
            baseUrl: '',
            login: '',
            password: '',
            instanceId: '',
            secretOrToken: ''
        }
    },
    methods: {
        getOrganizations (name){
            if(this.organizationSearch === '') this.organizationSelected = {}
            axios.get("/dc/organizations", {params: {name: name}})
            .then(response => {
                this.organizations = response.data
            })
            .catch(e => {
                this.errors.push(e)
            })
        },
        displayingResultOfOrganizationSearch(organization){
            return `${organization.denominationUniteLegale}, ${organization.siret}`
        },
        cloneConnector (){
            axios.post(`/configuration/connectors/${this.selectedConnector.id}/clone`, this.selectedConnector)
            .then(response => {
                this.$router.push({name: 'connectors'})
            })
            .catch(e => {
                this.errors.push(e)
            })
        },
        backToConnectorsManagament (){
            this.$router.push({name: 'connectors'})
        }
    }
}
</script>

<style scoped>
.passOk {
    background-color: #99ffd6;
    border-radius: 10px;
}
.passNok {
    background-color: #ffcccc;
    border-radius: 10px;
}
</style>


