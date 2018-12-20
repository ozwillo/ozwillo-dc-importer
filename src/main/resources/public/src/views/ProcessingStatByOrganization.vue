<template>
    <div class="container">
        <h2>Processing Statistics by Organizations</h2>
        <div class="form-group row">
            <label for="claimer-stat-display-date" class="col-sm-3 col-form-label col-form-label-sm">
                From: 
            </label>
            <input type="date" id="claimer-stat-display-date" v-model="datePicker"/>
        </div>
        <div class="form-group row">
            <div class="input-group">
                <label for="select-organization" class="col-sm-3 col-form-label col-form-label-sm">
                    Organization:
                </label>
                <select id="select-organization" class="form-control" v-model="siret">
                    <option></option>
                    <option v-for="organization in generalResume.resumeByOrganization">{{organization.orgSiret}}</option>
                </select>
                <div class="input-group-append">
                    <span class="input-group-text">{{organizationSelected.denominationUniteLegale}}</span>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-sm-3"></div>
            <div class="col-sm">
                <vue-bootstrap-typeahead
                id="claimer-organization"
                ref="claimerorganization"
                placeholder="Find an organization"
                v-model="organizationSearch"
                :append="organizationSelected.siret"
                :data="organizations"
                :serializer="displayingResultOfOrganizationSearch"
                @hit="organizationSelected = $event"/>
            </div>
        </div>
        <div class="row">
            <p>Processed since {{date | formatDate}}:</p>
        </div>
        <div class="form-group row" v-if="organizationSearch !== ''">
            <label for="organization-model-list" class="col-sm-3 col-form-label col-form-label-sm">
                Models processed for selected Organization:
            </label>
            <select id="organization-model-list" class="form-control-sm" v-model="selectedModelForOrganization">
                <option>All</option>
                <option v-for="model in resumeOrganization.modelResume">{{model.modelName}}</option>
            </select>
        </div>
        <div class="form-group row" v-if="organizationSearch !== ''">
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div>DC Importer has registered</div> 
                    <div class="secondary-displayed-stat-value">{{nbreActiveModelForOrganization}}</div> 
                    <div>{{displayedModelForOrganization}}</div>
                    <div>for Organization</div>
                    <div>{{organizationSelected.denominationUniteLegale}}</div>
                </div>
            </div>
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div>With</div>
                    <div><span class="secondary-displayed-stat-value">{{nbreCreatedModelForOrganization}}</span> {{displayedCreationForOrganization}}</div>
                    <div><span class="secondary-displayed-stat-value">{{nbreDeletedModelForOrganization}}</span> {{displayedDeletionForOrganization}}</div>
                    <div><span class="secondary-displayed-stat-value">{{nbreOfUpdateForOrganization}}</span> {{displayedUpdateForOrganization}}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

import axios from 'axios'
import debounce from 'lodash/debounce'
import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
import '@/utils/filters'

export default {
    name: 'ProcessingStatByOrganization',
    components: {
        VueBootstrapTypeahead
    },
    data (){
        return {
            date: '',
            datePicker: '',
            generalResume: {},
            siret: '',
            organizationSelected: {},
            organizationSearch: '',
            organizations: [],
            resumeOrganization: {},
            selectedModelForOrganization: 'All',
            errors: [],
        }
    },
    watch: {
        datePicker (val, oldVal){
            this.date = new Date(val)
            this.getGeneralStats(this.date)
        },
        organizationSearch: debounce(function(name) { this.getOrganizations(name) }, 500),
        organizationSelected: {
            handler(val, oldVal){
                this.$refs.claimerorganization.$data.inputValue = val.denominationUniteLegale
                
                if(Object.keys(val).length !== 0){
                    this.siret = val.siret
                }else{
                    this.siret = ''
                }

                this.getResumeOrganization(val.siret)
            },
            deep: true
        },
        siret (val, oldVal){
            if(val !== ''){
                this.getOrganizationBySiret(val)
            }else{
                this.organizationSelected = {}
                this.organizationSearch = ''
            }
        }
    },
    computed: {
        nbreActiveModelForOrganization (){
            if(this.selectedModelForOrganization === 'All') return this.resumeOrganization.nbreActiveModel
            else return this.getResumeModelForOrganization(this.selectedModelForOrganization).nbreActive
        },
        displayedModelForOrganization (){
            if(this.selectedModelForOrganization === 'All' && this.nbreActiveModelForOrganization > 1) return 'distinct Models'
            else if(this.selectedModelForOrganization === 'All') return 'Model'
            else return this.selectedModelForOrganization
        },
        nbreCreatedModelForOrganization (){
            if(this.selectedModelForOrganization === 'All') return this.resumeOrganization.nbreOfCreation
            else return this.getResumeModelForOrganization(this.selectedModelForOrganization).nbreCreated
        },
        displayedCreationForOrganization (){
            if(this.nbreCreatedModelForOrganization > 1) return 'Creations'
            else return 'Creation'
        },
        nbreDeletedModelForOrganization (){
            if(this.selectedModelForOrganization === 'All') return this.resumeOrganization.nbreOfDeletion
            else return this.getResumeModelForOrganization(this.selectedModelForOrganization).nbreDeleted
        },
        displayedDeletionForOrganization (){
            if(this.nbreDeletedModelForOrganization > 1) return 'Deletions'
            else return 'Deletion'
        },
        nbreOfUpdateForOrganization (){
            if(this.selectedModelForOrganization === 'All') return this.resumeOrganization.nbreOfUpdate
            else return this.getResumeModelForOrganization(this.selectedModelForOrganization).nbreOfUpdate
        },
        displayedUpdateForOrganization (){
            if(this.nbreOfUpdateForOrganization > 1) return 'Updates'
            else return 'Update'
        }
    },
    created (){
        var dt = new Date()
        dt.setMonth(dt.getMonth() - 3)
        this.date = dt
        this.getGeneralStats(this.date)
    },
    methods: {
        getGeneralStats (date){
            axios.get('/api/stat-view/processing-resume', {params: {date: date}})
            .then(response => {
                this.generalResume = response.data
            })
            .catch(e => {
                this.errors.push(e)
            })
        },
        displayingResultOfOrganizationSearch(organization) {
            return `${organization.denominationUniteLegale}, ${organization.siret}`
        },
        getOrganizations(name) {
            if(this.organizationSearch === '') this.organizationSelected = {}
            axios.get('/dc/organizations', {params: {name: name}})
                .then(response => {
                    this.organizations = response.data
                })
                .catch(e => {
                    this.errors.push(e)
                })
        },
        getOrganizationBySiret (siret){
	            axios.get('/dc/organizations', {params: {siret: siret}})
	            .then(response => {
                    this.organizationSelected = response.data[0]
                    this.organizationSearch = response.data[0].denominationUniteLegale
	                this.$refs.claimerorganization.$data.inputValue = this.organizationSelected.denominationUniteLegale
	            })
	            .catch(e => {
	                this.errors.push(e)
                })
        },
        getResumeOrganization (siret){
            var selectOrganization = this.generalResume.resumeByOrganization.find(el => {
                return el.orgSiret === siret
            })
            if(typeof selectOrganization !== 'undefined')
                this.resumeOrganization = selectOrganization
            else
                this.resumeOrganization = {
                    nbreActiveModel: 0,
                    nbreDistinctModel: 0,
                    nbreOfCreation: 0,
                    nbreOfDeletion: 0,
                    nbreOfUpdate: 0
                }
        },
        getResumeModelForOrganization (modelName){
            var selectModel = this.resumeOrganization.modelResume.find(el => {
                return el.modelName === modelName
            })
            if(typeof selectModel !== 'undefined')
                return selectModel
            else
                return {
                    nbreActive: 0,
                    nbreCreated: 0,
                    nbreDeleted: 0,
                    nbreOfUpdate: 0,
                    nbreOrganization: 0
                }
        }
    }
}
</script>

<style scoped>
.general-stat{
    margin: 0.3%;
    border-radius: 5px;
    background-color:#2C55A2;
    color: #FFF;
    text-align: center;
}
.center-el{
    margin: 0 auto;
}
.center-text{
    margin: auto;
    padding: 5%;
}
.displayed-stat-value{
    font-size: 35px;
    font-weight: bold;
}
.secondary-displayed-stat-value{
    font-size: 20px;
    font-weight: bold;
}
</style>


