<template>
    <div id="container" class="container">
        <h2>{{ $t('dashboard') }}</h2>
        <table class="table table-sm">
            <thead class="thead-dark">
                <tr>
                    <th>{{ $t('dc_model') }}</th>
                    <th>{{ $t('since') }}</th>
                    <th>{{ $tc('organization') }}</th>
                    <th>{{ $t('state') }}</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="dataRequest in dataRequests">
                    <td>
                        <router-link :to="{ name: 'check', params: { id: dataRequest.id }}">
                            {{ dataRequest.model }}
                        </router-link>
                    </td>
                    <td>{{ dataRequest.creationDate | dateDistanceInWords }}</td>
                    <td>{{ dataRequest.organization }}</td>
                    <td>{{ dataRequest.state }}</td>
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
  background-color: #6f438e;
  border-color: #32383e;
}
</style>
