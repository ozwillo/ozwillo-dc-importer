<template>
    <div id="container" class="container">
        <h2>Dashboard</h2>
        <table class="table table-sm table-striped">
            <thead class="thead-dark">
                <tr>
                    <th>DC Model</th>
                    <th>Date</th>
                    <th>Organization</th>
                    <th>State</th>
                </tr>
            </thead>
            <tbody class="tbody-dashboard">
                <tr v-for="dataRequest in dataRequests">
                    <td>
                        <router-link :to="{ name: 'check', params: { id: dataRequest.id }}">
                            {{ dataRequest.model }}
                        </router-link>
                    </td>
                    <td>{{ dataRequest.creationDate | dateDistanceInWords }}</td>
                    <td>{{ dataRequest.organization }}</td>
                    <td class="state-col">{{ dataRequest.state }}</td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script>
    import axios from 'axios'
    import VueRouter from 'vue-router'
    import '@/utils/filters'

    export default {
        name: "Dashboard",
        data() {
            return {
                dataRequests: [],
                errors: []
            }
        },
        beforeCreate() {
            axios.get(`/api/data-access`, {params: {state: 'sent'}})
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
  font-weight: normal;
  background-color: #2C55A2;
  border-color: #CCC;
}
.tbody-dashboard {
    color: #4c4c4c;

}
.state-col {
    color: #E62984;
}
</style>
