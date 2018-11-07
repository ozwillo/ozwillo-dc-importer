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
                <table>
                    <tr id="single-select-with-label">
                        <td class="col-sm-3 col-form-label col-form-label-sm">
                            {{ labelName }}
                        </td>
                        <td class="single-select-input">
                            <vue-single-select 
                                v-model="model"
                                v-bind:options="models"
                                placeholder="Pick a model dataset"
                                v-bind:required="true"
                            ></vue-single-select>
                        </td>
                    </tr>
                </table>
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
                models: [
                    'orgfr:Organisation_0',
                    'org:Organization_0',
                    'marchepublic:Consultation_0',
                    'citizenreq:user_0',
                    'grant:association_0'
                ]   //TODO: get model list by an API
            }
        },
        computed: {
            disabled: function(){
                return(this.organization == '' || this.email == '' || this.model == '')
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
            }
        }
    }
</script>

<style scoped>
.single-select-input{
    padding-left: 140px;
    width: 400px;
}
</style>
