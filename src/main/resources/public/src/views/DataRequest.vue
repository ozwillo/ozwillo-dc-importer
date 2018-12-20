<template>
    <div id="container" class="container">
        <h2>{{ $t('request_access_to_dataset') }}</h2>
        <form>
            <div class="form-group row">
                <label for="claimer-organization" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $tc('organization') }}
                </label>
                <vue-bootstrap-typeahead
                    id="claimer-organization"
                    ref="claimerorganization"
                    :placeholder="$t('search_organization')"
                    :minMatchingChars="0"
                    v-model="organizationSearch"
                    :append="organizationSelected.siret"
                    :data="organizations"
                    :serializer="displayingResultOfOrganizationSearch"
                    @hit="organizationSelected = $event"/>
            </div>
            <div class="form-group row">
                <label for="claimer-email" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $t('email') }}
                </label>
                <input id="claimer-email" v-model="dataAccessRequest.email"/>
            </div>
            <div class="form-group row">
                <label for="claimer-model" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $tc('model', 1) }}
                </label>
                <vue-bootstrap-typeahead
                    id="claimer-model"
                    ref="claimermodel"
                    :placeholder="$t('search_model')"
                    v-model="modelSearch"
                    :data="models"
                    :serializer="displayingResultOfModelSearch"
                    @hit="handleModelSelected($event)"/>
            </div>
            <div class="form-group row" v-if="modelSelected != null">
                <label for="fields-model" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $tc('field') }}
                </label>
                <ul id="fields-model" class="list-group">
                    <li class="list-group-item" v-for="field in modelSelected.fields">
                        <input type="checkbox" :id="field.name" v-model="field.requested">
                        <label :for="field.name">{{ field.name }}</label>
                    </li>
                </ul>
            </div>
            <button class="btn btn-outline-primary" type="button" @click="createDataAccessRequest" v-bind:disabled="disabled">$t('save')</button>
        </form>
    </div>
</template>

<script>
    import Vue from 'vue'
    import axios from 'axios'
    import debounce from 'lodash/debounce'
    import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
    import VueRouter from 'vue-router'

    //TODO : Find better than $ref... for displaying specific value in vue-bootstrap-typeahead input

    export default {
        name: "dataAccessRequest",
        components: {
            VueBootstrapTypeahead
        },
        data() {
            return {
                dataAccessRequest: {
                    id: null,
                    nom: '',
                    email: '',
                    organization: '',
                    model: ''
                },
                errors: [],
                organizationSelected: {},
                organizationSearch: '',
                organizations: [],
                modelSelected: null,
                modelSearch: '',
                models: []
            }
        },
        watch: {
            organizationSearch: debounce(function(name) { this.getOrganizations(name) }, 500),
            modelSearch: debounce(function(name) { this.getModels(name) }, 500),
            organizationSelected: {
                handler(val, oldVal){
                    this.$refs.claimerorganization.$data.inputValue = val.denominationUniteLegale
                },
                deep: true
            }
        },
        computed: {
            disabled: function(){
                return (this.organizationSelected === false || this.dataAccessRequest.email === '' || this.modelSelected === null)
            }
        },
        beforeCreate() {
            if(this.$route.params.id != null) {
                axios.get(`/api/data-access/${this.$route.params.id}`)
                  .then(response => {
                      this.dataAccessRequest = response.data
                      this.modelSelected = this.dataAccessRequest.model
                      this.$refs.claimermodel.$data.inputValue = this.dataAccessRequest.model

                      this.updateOrganizationBySiret()
                  })
                  .catch(e => {
                      this.errors.push(e)
                  })
            }
        },
        beforeRouteUpdate (to, from, next) {
            next()
            this.dataAccessRequest = {
                id: null,
                nom: '',
                email: '',
                organization: '',
                model: ''}
            
            this.organizationSelected = {}
            this.modelSelected = ''

            this.$refs.claimermodel.$data.inputValue = this.dataAccessRequest.model
            this.$refs.claimerorganization.$data.inputValue = this.organizationSearch
        },
        methods: {
            handleModelSelected(model) {
                model.fields.map(field => Vue.util.defineReactive(field, 'requested' , true))
                this.modelSelected = model
            },
            displayingResultOfModelSearch(model) {
                return model.name
            },
            displayingResultOfOrganizationSearch(organization) {
                return `${organization.denominationUniteLegale}, ${organization.siret}`
            },
            createDataAccessRequest() {
                Object.assign(this.dataAccessRequest, {
                    organization: this.organizationSelected.uri,
                    model: this.modelSelected.id,
                    fields: this.modelSelected.fields
                })
                axios.post(`/api/data-access`, this.dataAccessRequest)
                    .then(() => {
                        this.$router.push({ name: 'dashboard' })
                    })
                    .catch(e => {
                        this.errors.push(e)
                    })
            },
            getModels(name) {
                if(this.modelSearch.length <= 0) this.modelSelected = null
                axios.get('/dc/models', {params: {name: name}})
                    .then(response => {
                        this.models = response.data
                    })
                    .catch(e => {
                        this.errors.push(e)
                    })
            },
            getModel(type) {
                axios.get(`/dc/model/${type}`)
                    .then(response => {
                        this.modelSelected = response
                    })
                    .catch(e => {
                        this.errors.push(e)
                    })
            },
            getOrganizations(name) {
                axios.get('/dc/organizations', {params: {name: name}})
                    .then(response => {
                        this.organizations = response.data
                    })
                    .catch(e => {
                        this.errors.push(e)
                    })
            },
            updateOrganizationBySiret (){
	                var splitedUri = this.dataAccessRequest.organization.split("/")
	                var siret = splitedUri[splitedUri.length - 1]

	                axios.get('/dc/organizations', {params: {siret: siret}})
	                .then(response => {
	                    this.organizationSelected = response.data[0]
	                    this.$refs.claimerorganization.$data.inputValue = this.organizationSelected.denominationUniteLegale
	                })
	                .catch(e => {
	                    this.errors.push(e)
                    })
            }
        }
    }
</script>

<style scoped>

</style>
