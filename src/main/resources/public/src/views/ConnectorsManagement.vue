<template>
    <div class="container">
        <h2>{{ $t('connectors_management') }}</h2>
        <form>
            <div class="form-group row">
                <label for="cm-claimer-organization" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $tc('organization') }}
                </label>
                <vue-bootstrap-typeahead 
                id="cm-claimer-organization"
                ref="cmclaimerorganization"
                :placeholder="$t('search_by_organization')"
                v-model="organizationSearch"
                :data="organizations"
                :serializer="displayingResultOfOrganizationSearch"
                :append="organizationSelected.siret"
                @hit="organizationSelected = $event"
                @input="checkOrganizationSearch"/>
            </div>
            <div class="form-group row">
                <label for="cm-claimer-appName" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $t('application_name') }}
                </label>
                <input 
                    id="cm-claimer-appName"
                    :placeholder="$t('search_by_application')"
                    v-model="appName"/>
            </div>
            <div class="form-group row">
                <table class="table table-sm">
                    <thead class="thead-dark">
                        <tr>
                            <th>{{ $t('application_name') }}</th>
                            <th>{{ $t('organization_name') }}</th>
                            <th>{{ $t('siret_number') }}</th>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody class="tbody-connector-management">
                        <tr v-for="connector in connectors">
                            <td>{{connector.displayName}}</td>
                            <td>{{findOrganizationNameInMap(connector.organizationSiret)}}</td>
                            <td>{{connector.organizationSiret}}</td>
                            <td>
                                <router-link :to="{ name: 'clone', params: { id: connector.id, appName: connector.applicationName, siret: connector.organizationSiret }}" title="clone connector to an other organization">
                                    ++
                                </router-link>
                            </td>
                            <td>
                                <input type="button" class="delete-button" value="X" @click="deleteConnector(connector.id)" title="delete connector"/>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </form>
    </div>
</template>

<script>
import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
import axios from 'axios'
import debounce from 'lodash/debounce'

export default {
    name: 'ConnectorsManagement',
    components: {
        VueBootstrapTypeahead
    },
    data (){
        return {
            organizations: [],
            organizationSearch: '',
            organizationSelected: {},
            connectors: [],
            siret: '',
            siretList: [],
            siretAndOrgNameMaps: [],
            appName: '',
            errors: []
        }
    },
    watch: {
        organizationSearch: debounce(function(name){this.getOrganizations(name)}, 500),
        appName: debounce(function(appName){this.getConnectors(this.siret, appName)}, 500),
        organizationSelected: {
            handler (val, oldVal){
                this.$refs.cmclaimerorganization.$data.inputValue = val.denominationUniteLegale
                this.siret = this.organizationSelected.siret
                this.getConnectors(this.siret, this.appName)
            },
            deep: true
        }
    },
    created (){
        this.initializeConnectors()
    },
    methods: {
        checkOrganizationSearch (){
            if(this.organizationSearch === ''){
                this.organizationSelected = {
                    siret: ''
                }
            }
        },
        findOrganizationName (connectors){
            this.siretAndOrgNameMaps = []
                connectors.forEach(element => {
                    if(this.siretList.indexOf(element.organizationSiret) < 0)
                    this.siretList.push(element.organizationSiret)
                })
                this.siretList.forEach(siret => {
                    axios.get('/dc/organizations', {params: {siret: siret}})
                    .then(response => {
                        var siretAndOrgNameMap = {
                            siret: siret,
                            orgName: response.data[0].denominationUniteLegale
                        }
                        this.siretAndOrgNameMaps.push(siretAndOrgNameMap)
                    })
                    .catch(e => {
                        this.errors.push(e)
                    })
                })
        },
        findOrganizationNameInMap (siret){
            var organization = this.siretAndOrgNameMaps.find( o => {
                    return o.siret === siret
                })
            //Use stored mapping of siret and name to display organization name improve responsiveness
            //But Map not always fully initialized when tab is displayed and findOrganizationName is called
            if(typeof organization !== 'undefined')
                return organization.orgName
            else
                return ''
        },
        displayingResultOfOrganizationSearch (organization){
            return `${organization.denominationUniteLegale}, ${organization.siret}`
        },
        getOrganizations (name){
            axios.get('/dc/organizations', {params: {name: name}})
            .then(response => {
                this.organizations = response.data
            })
            .catch(e => {
                this.errors.push(e)
            })
        },
        initializeConnectors (){
            axios.get('/configuration/connectors')
                .then(response => {
                    this.connectors = response.data
                    this.findOrganizationName(response.data)
                })
                .catch(e => {
                    this.errors.push(e)
                })
        },
        getConnectors (siret, application){
            this.connectors = []
            axios.get('/configuration/connectors', {params: {siret, application}})
                .then(response => {
                    this.connectors = response.data
                })
                .catch(e => {
                    this.errors.push(e)
                })
        },
        deleteConnector (id){
            axios.delete(`/configuration/connectors/${id}`)
            .then(() => {
                this.initializeConnectors()
            })
            .catch(e => {
                this.errors.push(e)
            })
        }
    }
}
</script>

<style scoped>
.delete-button{
    color: red;
    background-color: white;
    border: 2px solid red;
    border-radius: 25px;
    cursor: pointer;
}
.table .thead-dark th {
  color: #fff;
  font-weight: normal;
  background-color: #2C55A2;
  border-color: #CCC;
}
.tbody-connector-management {
    color: #4c4c4c;

}
</style>


