<template>
    <div class="container">
        <h2>Processing Statistics by Model</h2>
        <div class="form-group row">
            <label for="claimer-stat-display-date" class="col-sm-3 col-form-label col-form-label-sm">
                From: 
            </label>
            <input type="date" id="claimer-stat-display-date" v-model="datePicker"/>
        </div>
        <div class="form-group row">
            <div class="input-group">
                <label for="select-model" class="col-sm-3 col-form-label col-form-label-sm">
                    {{ $tc('model') }} 
                </label>
                <select id="select-model" v-model="modelSelected" title="Select a model in list">
                    <option></option>
                    <option v-for="model in generalResume.resumeByModel">{{model.modelName}}</option>
                </select>
                <vue-bootstrap-typeahead
                    id="claimer-model"
                    ref="claimermodel"
                    title="Search a model among all DC Models"
                    :placeholder="$t('search_model')"
                    v-model="modelSearch"
                    :data="models"
                    :serializer="displayingResultOfModelSearch"
                    @hit="modelSelected = $event.name"/>
            </div>
        </div>
        <div class="row">
            <p>{{ $tc('processing_since', this.generalResume.nbreProcessing) }} {{date | formatDate}}:</p>
        </div>
        <div class="form-group row" v-if="modelSelected !==''">
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div class="displayed-stat-value">{{resumeModel.nbreActive}}</div>
                    <div>{{modelSelected}}</div>
                    <div>{{ $t('being_processed') }}</div>
                </div>
            </div>
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div><span class="secondary-displayed-stat-value">
                            {{ this.resumeModel.nbreCreated }}
                        </span>
                        {{ $tc('creation', this.resumeModel.nbreCreated) }}
                    </div>
                    <div><span class="secondary-displayed-stat-value">
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
            <div class="general-stat col-sm-3">
                <div class="center-text">
                    <div class="displayed-stat-value">{{resumeModel.nbreOrganization}}</div> 
                    <div>{{ $tc('organization') }}</div>
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
    name: 'ProcessingStatByModel',
    components: {
        VueBootstrapTypeahead
    },
    data (){
        return {
            date: '',
            datePicker: '',
            generalResume: {},
            modelSelected: '',
            modelSearch: '',
            models: [],
            resumeModel: {},
            errors: []
        }
    },
    watch: {
        datePicker (val, oldVal){
            this.date = new Date(val)
            this.getGeneralStats(this.date)
        },
        modelSearch: debounce(function(name) { this.getModels(name) }, 500),
        modelSelected (val, oldVal){
            this.$refs.claimermodel.$data.inputValue = val
            this.getResumeModel(val)
        },
        generalResume (val, oldVal){
            this.getResumeModel(this.modelSelected)
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
        displayingResultOfModelSearch(model) {
            return model.name
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


