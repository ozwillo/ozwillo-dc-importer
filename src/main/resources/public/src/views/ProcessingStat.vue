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
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div class="displayed-stat-value">{{ this.generalResume.nbreProcessing }}</div>
                    <div>{{ $tc('processing_completed', this.generalResume.nbreProcessing) }}</div>
                    <div>{{ $t('since') }} {{ date | formatDate }}</div>
                </div>
            </div>
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div class="displayed-stat-value">{{ this.generalResume.nbreDistinctModel }}</div>
                    <div>{{ $tc('model', this.generalResume.nbreDistinctModel) }}</div>
                </div>
            </div>
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div class="displayed-stat-value">{{ this.generalResume.nbreDistinctOrg }}</div>
                    <div>{{ $tc('organization', this.generalResume.nbreDistinctOrg) }}</div>
                </div>
            </div>
        </div>
        <div class="form-group row">
            <div class="col-sm-5">
                <label for="model-pie">{{ $tc('processed_model', pieData.length) }}</label>
                <pie-chart id="model-pie" :data="pieData" :donut="true" :colors="colors" legend="right"></pie-chart>
            </div>
            <div class="col-sm-5">
                <label for="organization-column">{{ $tc('concerned_organization', columnData.length) }}</label>
                <column-chart id="organization-column" :data="columnData" :colors="colors" :legend="false"></column-chart>
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
            colors: [],
            pieData: [],
            columnData: [],
            errors: []
        }
    },
    watch: {
        datePicker (val, oldVal){
            this.date = new Date(val)
            this.getGeneralStats(this.date)
        },
        generalResume (val, oldVal){
            this.pieData = this.countProcessByModel()
            this.columnData = this.countProcessByOrganization()
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
        this.colors = ['#2c55a2', '#e62984', '#4c6eb0', '#eb549d', '#5a4a9a', '#a5388e', '#7690c3', '#ef74af', '#644899', '#893f93', '#96aad0', '#f49fc8', '#4c4c4c', '#e4e4e4']  
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
                
                axios.get('/dc/organizations', {params: {siret: el.orgSiret}})
                .then(response => {
                    nameAndNbreProcess.push(response.data[0].denominationUniteLegale)
                    nameAndNbreProcess.push(el.nbreProcessing)
                    processByOrganization.push(nameAndNbreProcess)
                })
                .catch(e => {
                    this.erros.push(e)
                })
            })

            return processByOrganization
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


