<template>
    <div class="container">
        <h2>Processing Statistics</h2>
        <div class="form-group row">
            <div class="col">
                <label for="claimer-stat-display-date" class="col-sm-3 col-form-label col-form-label-sm">
                    From: 
                </label>
                <input type="date" id="claimer-stat-display-date" v-model="datePicker"/>
            </div>
        </div>
        <div class="form-group row">
            <div class="general-stat">
                <div class="center-text">
                    <div>DC Importer has completed</div>
                    <div class="displayed-stat-value">{{generalNbreProcess}}</div> 
                    <div>{{displayedProcessing}} since</div>
                    <div>{{date | formatDate}}</div>
                </div>
            </div>
            <div class="general-stat">
                <div class="center-text">
                    <div>On</div>
                    <div class="displayed-stat-value">{{generalDistinctModel}}</div>
                    <div>{{displayedDistinctGeneralModel}}</div> 
                </div>
            </div>
            <div class="general-stat">
                <div class="center-text">
                    <div>For </div>
                    <div class="displayed-stat-value">{{generalDistinctOrganization}}</div> 
                    <div>{{displayedDistinctGeneralOrganization}}</div>
                </div>
            </div>
        </div>
        <div class="row">
            <p>Processed since {{date | formatDate}}:</p>
        </div>
        <div class="form-group row">
            <div class="col">
                <label for="select-model" class="col-sm-3 col-form-label col-form-label-sm">
                    Models: 
                </label>
                <select id="select-model" class="form-control-sm" v-model="modelSelected">
                    <option></option>
                    <option v-for="model in generalResume.resumeByModel">{{model.modelName}}</option>
                </select>
            </div>
            <div class="col">
                <label for="select-organization" class="col-sm-3 col-form-label col-form-label-sm">
                    Organization:
                </label>
                <select id="select-organization" class="form-control-sm" v-model="siret">
                    <option></option>
                    <option v-for="organization in generalResume.resumeByOrganization">{{organization.orgSiret}}</option>
                </select>
                <input type="text" class="form-control-sm" disabled=true v-model="organizationSelected.denominationUniteLegale"/>
            </div>
        </div>
        <div class="row">
            Search:
        </div>
        <div class="form-group row">
            <label for="claimer-model" class="col-sm-3 col-form-label col-form-label-sm">
                By Model: 
            </label>
            <vue-bootstrap-typeahead
                id="claimer-model"
                ref="claimermodel"
                placeholder="Find a model of dataset"
                v-model="modelSearch"
                :data="models"
                :serializer="displayingResultOfModelSearch"
                @hit="modelSelected = $event.name"/>
        </div>
        <div class="form-group row" v-if="modelSelected !== ''">
            <div class="general-stat">
                <div class="center-text">
                    <div>DC Importer has registered</div>
                    <div class="displayed-stat-value">{{nbreActiveModelForModel}}</div> 
                    <div>active</div>
                    <div>{{modelSelected}}</div>
                </div>
            </div>
            <div class="general-stat">
                <div class="center-text">
                    <div>With</div>
                    <div><span class="secondary-displayed-stat-value">{{nbreCreatedModelForModel}}</span> {{displayedCreationForModel}}</div>
                    <div><span class="secondary-displayed-stat-value">{{nbreDeletedModelForModel}}</span> {{displayedDeletionForModel}}</div>
                    <div><span class="secondary-displayed-stat-value">{{nbreOfUpdateForModel}}</span> {{displayedUpdateForModel}}</div>
                </div>
            </div>
            <div class="general-stat">
                <div class="center-text">
                    <div>For</div> 
                    <div class="displayed-stat-value">{{nbreOrganizationForModel}}</div> 
                    <div>{{displayedOrganizationForModel}}</div>
                </div>
            </div>
        </div>
        <div class="form-group row">
            <label for="claimer-organization" class="col-sm-3 col-form-label col-form-label-sm">
                By Organization:
            </label>
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
            <div class="general-stat">
                <div class="center-text">
                    <div>DC Importer has registered</div> 
                    <div class="secondary-displayed-stat-value">{{nbreActiveModelForOrganization}}</div> 
                    <div>{{displayedModelForOrganization}}</div>
                    <div>for Organization</div>
                    <div>{{organizationSelected.denominationUniteLegale}}</div>
                </div>
            </div>
            <div class="general-stat">
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
    name: 'ProcessingStat',
    components: {
        VueBootstrapTypeahead
    },
    data (){
        return {
            generalResume: {},
            date: '',
            datePicker: '',
            modelSelected: '',
            modelSearch: '',
            models: [],
            resumeModel: {},
            organizationSelected: {},
            organizationSearch: '',
            organizations: [],
            resumeOrganization: {},
            selectedModelForOrganization: 'All',
            siret: '',
            errors: []
        }
    },
    watch: {
        modelSearch: debounce(function(name) { this.getModels(name) }, 500),
        organizationSearch: debounce(function(name) { this.getOrganizations(name) }, 500),
        organizationSelected: {
            handler(val, oldVal){
                this.$refs.claimerorganization.$data.inputValue = val.denominationUniteLegale
                this.siret = val.siret
                this.getResumeOrganization(val.siret)
            },
            deep: true
        },
        datePicker (val, oldVal){
            this.date = new Date(val)
            this.getGeneralStats(this.date)
        },
        modelSelected (val, oldVal){
            this.$refs.claimermodel.$data.inputValue = val
            this.getResumeModel(val)
        },
        generalResume (val, oldVal){
            this.getResumeModel(this.modelSelected)
        },
        siret (val, oldVal){
            this.getOrganizationBySiret(val)
        }
    },
    computed: {
        generalNbreProcess (){
            return this.generalResume.nbreProcessing
        },
        generalDistinctModel (){
            return this.generalResume.nbreDistinctModel
        },
        generalDistinctOrganization (){
            return this.generalResume.nbreDistinctOrg
        },
        nbreActiveModelForModel (){
            return this.resumeModel.nbreActive
        },
        nbreCreatedModelForModel (){
            return this.resumeModel.nbreCreated
        },
        nbreDeletedModelForModel (){
            return this.resumeModel.nbreDeleted
        },
        nbreOfUpdateForModel (){
            return this.resumeModel.nbreOfUpdate
        },
        nbreOrganizationForModel (){
            return this.resumeModel.nbreOrganization
        },
        nbreActiveModelForOrganization (){
            if(this.selectedModelForOrganization === 'All') return this.resumeOrganization.nbreActiveModel
            else return this.getResumeModelForOrganization(this.selectedModelForOrganization).nbreActive
        },
        nbreCreatedModelForOrganization (){
            if(this.selectedModelForOrganization === 'All') return this.resumeOrganization.nbreOfCreation
            else return this.getResumeModelForOrganization(this.selectedModelForOrganization).nbreCreated
        },
        nbreDeletedModelForOrganization (){
            if(this.selectedModelForOrganization === 'All') return this.resumeOrganization.nbreOfDeletion
            else return this.getResumeModelForOrganization(this.selectedModelForOrganization).nbreDeleted
        },
        nbreOfUpdateForOrganization (){
            if(this.selectedModelForOrganization === 'All') return this.resumeOrganization.nbreOfUpdate
            else return this.getResumeModelForOrganization(this.selectedModelForOrganization).nbreOfUpdate
        },
        displayedProcessing (){
            if(this.generalNbreProcess > 1) return 'processings' 
            else return 'processing'
        },
        displayedDistinctGeneralModel (){
            if(this.generalDistinctModel > 1) return 'distinct DC Models'
            else return 'Model'
        },
        displayedDistinctGeneralOrganization (){
            if(this.generalDistinctOrganization > 1) return 'different Organizations'
            else return 'Organization'
        },
        displayedCreationForModel (){
            if(this.nbreCreatedModelForModel > 1) return 'Creations'
            else return 'Creation'
        },
        displayedDeletionForModel (){
            if(this.nbreDeletedModelForModel > 1) return 'Deletions'
            else return 'Deletion'
        },
        displayedUpdateForModel (){
            if(this.nbreOfUpdateForModel > 1) return 'Updates'
            else return 'Update'
        },
        displayedOrganizationForModel (){
            if(this.nbreOrganizationForModel > 1) return 'different Organizations'
            else return 'Organization'
        },
        displayedModelForOrganization (){
            if(this.selectedModelForOrganization === 'All' && this.nbreActiveModelForOrganization > 1) return 'distinct Models'
            else if(this.selectedModelForOrganization === 'All') return 'Model'
            else return this.selectedModelForOrganization
        },
        displayedCreationForOrganization (){
            if(this.nbreCreatedModelForOrganization > 1) return 'Creations'
            else return 'Creation'
        },
        displayedDeletionForOrganization (){
            if(this.nbreDeletedModelForOrganization > 1) return 'Deletions'
            else return 'Deletion'
        },
        displayedUpdateForOrganization (){
            if(this.nbreOfUpdateForOrganization > 1) return 'Updates'
            else return 'Update'
        }
    },
    created () {
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
        getModels(name) {
            if(this.modelSearch.length <= 0) this.modelSelected = ''
            axios.get('/dc/models', {params: {name: name}})
                .then(response => {
                    this.models = response.data
                })
                .catch(e => {
                    this.errors.push(e)
                })
        },
        displayingResultOfModelSearch(model) {
            return model.name
        },
        getResumeModel (modelName){
            var selectModel = this.generalResume.resumeByModel.find(el => {
                return el.modelName === modelName
            })
            if(typeof selectModel !== 'undefined')
                this.resumeModel = selectModel
            else
                this.resumeModel = {
                    nbreActive: 0,
                    nbreCreated: 0,
                    nbreDeleted: 0,
                    nbreOfUpdate: 0,
                    nbreOrganization: 0
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
        displayingResultOfOrganizationSearch(organization) {
            return `${organization.denominationUniteLegale}, ${organization.siret}`
        }
    }
}
</script>

<style scoped>
.general-stat{
    margin: 5px;
    width: 250px;
    height: 150px;
    border-radius: 5px;
    background-color:#4c2d62;
    color: #fff;
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


