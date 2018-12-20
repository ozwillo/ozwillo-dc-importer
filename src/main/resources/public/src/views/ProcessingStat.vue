<template>
    <div class="container">
        <h2>{{ $t('processing_statistics') }}</h2>
        <div class="form-group row">
            <div class="col">
                <label for="claimer-stat-display-date" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $t('since') }}
                </label>
                <input type="date" id="claimer-stat-display-date" v-model="datePicker"/>
            </div>
        </div>
        <div class="form-group row">
            <div class="general-stat">
                <div class="center-text">
                    <div class="displayed-stat-value">{{ this.generalResume.nbreProcessing }}</div>
                    <div>{{ $tc('processing_completed', this.generalResume.nbreProcessing) }}</div>
                    <div>{{ $t('since') }} {{ date | formatDate }}</div>
                </div>
            </div>
            <div class="general-stat">
                <div class="center-text">
                    <div class="displayed-stat-value">{{ this.generalResume.nbreDistinctModel }}</div>
                    <div>{{ $tc('model', this.generalResume.nbreDistinctModel) }}</div>
                </div>
            </div>
            <div class="general-stat">
                <div class="center-text">
                    <div class="displayed-stat-value">{{ this.generalResume.nbreDistinctOrg }}</div>
                    <div>{{ $tc('organization', this.generalResume.nbreDistinctOrg) }}</div>
                </div>
            </div>
        </div>
        <div class="form-group row">
            <div class="col">
                <label for="model-pie">{{ $tc('processed_model', pieData.length) }}</label>
                <pie-chart id="model-pie" :data="pieData" :donut="true" width="500px" :colors="colors" legend="right"></pie-chart>
            </div>
            <div class="col">
                <label for="organization-column">{{ $tc('concerned_organization', columnData.length) }}</label>
                <column-chart id="organization-column" :data="columnData" width="500px" :colors="colors" :legend="false"></column-chart>
            </div>
        </div>
        <div class="row">
            <p>{{ $tc('processing_since', this.generalResume.nbreProcessing) }} {{date | formatDate}}</p>
        </div>
        <div class="form-group row">
            <div class="col">
                <label for="select-model" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $tc('model') }}
                </label>
                <select id="select-model" class="form-control-sm" v-model="modelSelected">
                    <option></option>
                    <option v-for="model in generalResume.resumeByModel">{{model.modelName}}</option>
                </select>
            </div>
            <div class="col">
                <label for="select-organization" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $tc('organization') }}
                </label>
                <select id="select-organization" class="form-control-sm" v-model="siret">
                    <option></option>
                    <option v-for="organization in generalResume.resumeByOrganization">{{organization.orgSiret}}</option>
                </select>
                <input type="text" class="form-control-sm" disabled=true v-model="organizationSelected.denominationUniteLegale"/>
            </div>
        </div>
        <div class="row">
            {{ $t('search') }}
        </div>
        <div class="form-group row">
            <label for="claimer-model" class="col-sm-3 col-form-label col-form-label-sm">
                {{ $t('by_model') }}
            </label>
            <vue-bootstrap-typeahead
                id="claimer-model"
                ref="claimermodel"
                :placeholder="$t('search_model')"
                v-model="modelSearch"
                :data="models"
                :serializer="displayingResultOfModelSearch"
                @hit="modelSelected = $event.name"/>
        </div>
        <div class="form-group row" v-if="modelSelected !== ''">
            <div class="general-stat">
                <div class="center-text">
                    <div class="displayed-stat-value">{{ this.resumeModel.nbreActive }}</div>
                    <div>{{modelSelected}}</div>
                    <div>{{ $t('being_processed') }}</div>
                </div>
            </div>
            <div class="general-stat">
                <div class="center-text">
                    <div>
                        <span class="secondary-displayed-stat-value">
                            {{ this.resumeModel.nbreCreated }}
                        </span>
                        {{ $tc('creation', this.resumeModel.nbreCreated) }}
                    </div>
                    <div>
                        <span class="secondary-displayed-stat-value">
                            {{ this.resumeModel.nbreDeleted }}
                        </span>
                        {{ $tc('deletion', this.resumeModel.nbreCreated) }}
                    </div>
                    <div>
                        <span class="secondary-displayed-stat-value">
                            {{ this.resumeModel.nbreOfUpdate }}
                        </span>
                        {{ $tc('update', this.resumeModel.nbreCreated) }}
                    </div>
                </div>
            </div>
            <div class="general-stat">
                <div class="center-text">
                    <div class="displayed-stat-value">{{ this.resumeModel.nbreOrganization }}</div>
                    <div>{{ $tc('organization') }}</div>
                </div>
            </div>
        </div>
        <div class="form-group row">
            <label for="claimer-organization" class="col-sm-3 col-form-label col-form-label-sm">
                {{ $t('by_organization') }}
            </label>
            <vue-bootstrap-typeahead
                id="claimer-organization"
                ref="claimerorganization"
                :placeholder="$t('search_by_organization')"
                v-model="organizationSearch"
                :append="organizationSelected.siret"
                :data="organizations"
                :serializer="displayingResultOfOrganizationSearch"
                @hit="organizationSelected = $event"/>
        </div>
        <div class="form-group row" v-if="organizationSearch !== ''">
            <label for="organization-model-list" class="col-sm-3 col-form-label col-form-label-sm">
                {{ $t('models_processed_for_selected_organization') }}
            </label>
            <select id="organization-model-list" class="form-control-sm" v-model="selectedModelForOrganization">
                <option></option>
                <option v-for="model in resumeOrganization.modelResume">{{model.modelName}}</option>
            </select>
        </div>
        <div class="form-group row" v-if="organizationSearch !== ''">
            <div class="general-stat">
                <div class="center-text">
                    <div v-if="this.selectedModelForOrganization === 'All'">
                        <div class="secondary-displayed-stat-value">
                            {{ this.resumeOrganization.nbreActiveMode }}
                        </div>
                        <div>
                            {{ $tc('model', this.resumeOrganization.nbreActiveMode) }}
                        </div>
                    </div>
                    <div v-else>
                        <div class="secondary-displayed-stat-value">
                            {{ this.getResumeModelForOrganization(this.selectedModelForOrganization).nbreActive }}
                        </div>
                        <div>
                            {{ displayedModelForOrganization }}
                        </div>
                    </div>
                    <div>{{ $t('for_organization') }}</div>
                    <div>{{organizationSelected.denominationUniteLegale}}</div>
                </div>
            </div>
            <div class="general-stat">
                <div class="center-text">
                    <div>
                        <span class="secondary-displayed-stat-value">
                            {{ nbreCreatedModelForOrganization }}
                        </span>
                        {{ $tc('creation', nbreCreatedModelForOrganization) }}
                    </div>
                    <div>
                        <span class="secondary-displayed-stat-value">
                            {{ nbreDeletedModelForOrganization }}
                        </span>
                        {{ $tc('deletion', nbreDeletedModelForOrganization) }}
                    </div>
                    <div>
                        <span class="secondary-displayed-stat-value">
                            {{ nbreOfUpdateForOrganization }}
                        </span>
                        {{ $tc('update', nbreOfUpdateForOrganization) }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

import Vue from 'vue'
import VueChartkick from 'vue-chartkick'
import Chart from 'chart.js'
import axios from 'axios'
import debounce from 'lodash/debounce'
import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'
import '@/utils/filters'

Vue.use(VueChartkick, {adapter: Chart})

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
            colors: [],
            pieData: [],
            columnData: [],
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
            this.pieData = this.countProcessByModel()
            this.columnData = this.countProcessByOrganization()
            this.getResumeModel(this.modelSelected)
        },
        siret (val, oldVal){
            this.getOrganizationBySiret(val)
        }
    },
    computed: {
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
        displayedModelForOrganization (){
            if(this.selectedModelForOrganization !== 'All') return this.selectedModelForOrganization
        }
    },
    created () {
        var dt = new Date()
        dt.setMonth(dt.getMonth() - 3)
        this.date = dt
        this.getGeneralStats(this.date)
        this.colors = ['#4c2d62', '#a673e3', '#660066', '#9350dc', '#7a6490', '#802ed5', '#9d5ea9', '#c0a1ce', '#a068a9', '#a193b6', '#6c0cce', '#8c4592', '#b8aecc', '#79227c', '#663399', '#660066']  
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
        },
        countProcessByModel (){
            var processByModel = []
            this.generalResume.resumeByModel.forEach(el => {
                var nameAndNbreProcess = []
                nameAndNbreProcess.push(el.modelName)
                nameAndNbreProcess.push(el.nbreProcessing)
                processByModel.push(nameAndNbreProcess)
            })

            return processByModel
        },
        countProcessByOrganization (){
            var processByOrganization = []
            this.generalResume.resumeByOrganization.forEach(el => {
                var nameAndNbreProcess = []
                nameAndNbreProcess.push(el.orgSiret)
                nameAndNbreProcess.push(el.nbreProcessing)
                processByOrganization.push(nameAndNbreProcess)
            })

            return processByOrganization
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


