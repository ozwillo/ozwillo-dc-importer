<template>
    <div id="container" class="container">
        <h2>Dashboard</h2>
        <table class="table table-sm">
            <thead class="thead-dark">
                <tr>
                    <th>Nom</th>
                    <th>DC Model</th>
                    <th>Organization</th>
                    <th>State</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="dataRequest in dataRequests">
                    <td>
                        <router-link :to="{ name: 'request', params: { id: dataRequest.id }}">
                          {{ dataRequest.nom }}
                        </router-link>
                    </td>
                    <td>{{ dataRequest.model }}</td>
                    <td>{{ dataRequest.organization }}</td>
                    <td>{{ dataRequest.state }}</td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script>
    import axios from 'axios'
    import  VueRouter from 'vue-router'

    export default {
        name: "Dashboard",
        data() {
            return {
                dataRequests: [],
                errors: []
            }
        },
        beforeCreate() {
            axios.get(`/api/data_access_request/state/sent`)
                .then(response => {
                    this.dataRequests = response.data
                })
                .catch(e => {
                    this.errors.push(e)
                })
        }
    }
</script>

<style scoped>
.table .thead-dark th {
  color: #fff;
  background-color: #6f438e;
  border-color: #32383e;
}
</style>
