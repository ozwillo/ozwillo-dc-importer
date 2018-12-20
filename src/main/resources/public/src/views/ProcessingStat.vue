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
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div>DC Importer has completed</div>
                    <div class="displayed-stat-value">{{generalNbreProcess}}</div> 
                    <div>{{displayedProcessing}} since</div>
                    <div>{{date | formatDate}}</div>
                </div>
            </div>
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div>On</div>
                    <div class="displayed-stat-value">{{generalDistinctModel}}</div>
                    <div>{{displayedDistinctGeneralModel}}</div> 
                </div>
            </div>
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div>For </div>
                    <div class="displayed-stat-value">{{generalDistinctOrganization}}</div> 
                    <div>{{displayedDistinctGeneralOrganization}}</div>
                </div>
            </div>
        </div>
        <div class="form-group row">
            <div class="col-sm-5">
                <label for="model-pie">Processed Models: </label>
                <pie-chart id="model-pie" :data="pieData" :donut="true" :colors="colors" legend="right"></pie-chart>
            </div>
            <div class="col-sm-5">
                <label for="organization-column">Concerned Organizations</label>
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
        generalNbreProcess (){
            return this.generalResume.nbreProcessing
        },
        generalDistinctModel (){
            return this.generalResume.nbreDistinctModel
        },
        generalDistinctOrganization (){
            return this.generalResume.nbreDistinctOrg
        },
        nbreCreatedModelForModel (){
            return this.resumeModel.nbreCreated
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


