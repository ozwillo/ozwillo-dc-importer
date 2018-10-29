<template>
    <div id="container" class="container">
        <h2>Request for access to dataset</h2>
        <form>
            <div class="form-group row">
                <label for="claimer-collectivity" class="col-sm-3 col-form-label col-form-label-sm">
                    Collectivity
                </label>
                <input id="claimer-collectivity" value=""/>
            </div>
            <div class="form-group row">
                <label for="claimer-email" class="col-sm-3 col-form-label col-form-label-sm">
                    Email
                </label>
                <input id="claimer-email" v-model="email"/>
            </div>
        </form>
    </div>
</template>

<script>
    import axios from 'axios'
    export default {
        name: "DataRequest",
        data() {
            return {
                email: ''
            }
        },
        beforeCreate() {
          axios.get(`actuator/health`)
            .then(response => {
              this.response = response.data
              console.log(this.response)
            })
            .catch(e => {
              this.errors.push(e)
            })
        },
        methods: {
            callRestService() {
              event.preventPropagation()
            }
        }
    }
</script>

<style scoped>

</style>
