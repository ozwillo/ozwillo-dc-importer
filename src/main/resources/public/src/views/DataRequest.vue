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
                <label for="dc-model" class="col-sm-3 col-form-label col-form-label-sm">
                    DC Model
                </label>
                <input id="dc-model" v-model="model"/>
            </div>
            <input type="button" @click="createDataRequestModel()" value="submit">
        </form>
    </div>
</template>

<script>
    import axios from 'axios'
    export default {
        name: "DataRequest",
        data() {
            return {
                organization: '',
                email: '',
                model: ''
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

</style>
