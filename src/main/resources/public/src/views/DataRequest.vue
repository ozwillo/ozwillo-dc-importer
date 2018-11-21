<template>
    <div id="container" class="container">
        <h2>Request for access to dataset</h2>
        <form>
            <div class="form-group row">
                <label for="claimer-collectivity" class="col-sm-3 col-form-label col-form-label-sm">
                    Organization
                </label>
                <input id="claimer-collectivity" v-model="dataRequest.organization"/>
            </div>
            <div class="form-group row">
                <label for="claimer-email" class="col-sm-3 col-form-label col-form-label-sm">
                    Email
                </label>
                <input id="claimer-email" v-model="dataRequest.email"/>
            </div>
            <div class="form-group row">
                <label for="claimer-model" class="col-sm-3 col-form-label col-form-label-sm">
                    Model
                </label>
                <vue-single-select 
                    v-model="dataRequest.model"
                    v-bind:options="models"
                    placeholder="Pick a model dataset"
                    v-bind:required="true"/>
            </div>
            <input type="button" @click="createDataRequestModel()" value="submit" v-bind:disabled="disabled">
        </form>
    </div>
</template>

<script>
    import axios from 'axios'
    import VueSingleSelect from 'vue-single-select'
    import  VueRouter from 'vue-router'

    export default {
        name: "DataRequest",
        components: {
          VueSingleSelect  
        },
        data() {
            return {
                dataRequest: {
                    id: null,
                    nom: '',
                    email: '',
                    organization: '',
                    model: ''
                },
                models: [
                    'orgfr:Organisation_0',
                    'org:Organization_0',
                    'marchepublic:Consultation_0',
                    'citizenreq:user_0',
                    'grant:association_0'
                ],
                errors: [],
                response: {}
            }
        },
        computed: {
            disabled: function(){
                return(this.dataRequest.organization == '' || this.dataRequest.email == '' || (this.dataRequest.model == '' || this.dataRequest.model === null))
            }
        },
        beforeCreate() {
            if(this.$route.params.id !== null) {
                axios.get(`/api/data_access_request/${this.$route.params.id}`)
                  .then(response => {
                    this.dataRequest = response.data
                  })
                  .catch(e => {
                    this.errors.push(e)
                  })
            }
        },
        beforeRouteUpdate (to, from, next) {
            next()
            this.dataRequest = {}
        },
        methods: {
            createDataRequestModel() {
                axios.post(`/api/data_access_request/123456789/send`, this.dataRequest)
                    .then(response => {
                        this.response = response.data
                        this.$router.push({ name: 'dashboard' })
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
