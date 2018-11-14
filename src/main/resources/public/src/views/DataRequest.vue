<template>
    <div id="container" class="container">
        <h2>Request for access to dataset</h2>
        <form>
            <div class="form-group row">
                <label for="claimer-collectivity" class="col-sm-3 col-form-label col-form-label-sm">
                    Organization
                </label>
                <input id="claimer-collectivity" v-model="organization"/>
            </div>
            <div class="form-group row">
                <label for="claimer-email" class="col-sm-3 col-form-label col-form-label-sm">
                    Email
                </label>
                <input id="claimer-email" v-model="email"/>
            </div>
            <div class="form-group row">
                <label class="col-sm-3 col-form-label col-form-label-sm">{{ labelName }}</label>
                <vue-single-select 
                    v-model="model"
                    v-bind:options="models"
                    placeholder="Pick a model dataset"
                    v-bind:required="true"
                ></vue-single-select>
            </div>
            <input type="button" @click="createDataRequestModel()" value="submit" v-bind:disabled="disabled">
        </form>
    </div>
</template>

<script>
    import axios from 'axios'
    import VueSingleSelect from 'vue-single-select'
    export default {
        name: "DataRequest",
        components: {
          VueSingleSelect  
        },
        data() {
            return {
                organization: '',
                email: '',
                model: '',
                labelName: 'DC Model :',
                models: []   //TODO: get model list by an API
            }
        },
        beforeCreate() {
            axios.get('api/data_access_request/123456789/model')
                .then(response => {
                    this.models = response.data
                })
                .catch(e => {
                    this.errors.push(e)
                })
        },
        computed: {
            disabled: function(){
                return(this.organization == '' || this.email == '' || this.model == '')
            },
            something: function(value){
              this.getModels(value)
          }
        },
        methods: {
            callRestService() {
              event.preventPropagation()
            },
            createDataRequestModel() {
              axios.post(`api/data_access_request/123456789/send`, {
                nom: "",
                organization: this.organization,
                email: this.email,
                model: this.model
              })
                .then(response => {
                  this.response = response.data
                  console.log(this.response)
                })
                .catch(e => {
                  this.errors.push(e)
                })
            },
            getModels(name){
                axios.get('api/data_access_request/123456789/model?name=' + name)
                .then(response => {
                    this.models = response.data
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
