<template>
    <div class="container">
        <h2>Data access check</h2>
        <p>{{ dataRequest.model }}</p>
        <div class="form-check">
            <input id="agreeAllData" class="form-check-input" type="checkbox" :value="agreeAllData" @change="handleAgreeAllData">
            <label for="agreeAllData" class="form-check-label">
                Authorize access to all data of "{{ dataRequest.model }}" model
            </label>
        </div>
        <button class="btn btn-outline-primary" type="button" @click="validateAccess" :disabled="!agreeAllData">
            Valid
        </button>
        <button class="btn btn-outline-primary" type="button" @click="refuseAccess">
            Refuse
        </button>
    </div>
</template>

<script>
    import axios from 'axios'

    export default {
        name: 'CheckDataAccess',
        data() {
            return {
                dataRequest: {},
                errors: [],
                agreeAllData: false
            }
        },
        beforeCreate() {
            axios.get(`/api/data-access/${this.$route.params.id}`)
                .then(response => {
                    this.dataRequest = response.data
                })
                .catch(e => {
                    this.errors.push(e)
                })
        },
        methods: {
            handleAgreeAllData() {
              this.agreeAllData = !this.agreeAllData
            },
            validateAccess() {
                this.modifyStateOfRequest('valid', this.dataRequest.id)
            },
            refuseAccess() {
                this.modifyStateOfRequest('reject', this.dataRequest.id)
            },
            modifyStateOfRequest(action, id) {
                axios.put(`/api/data-access/${id}/${action}`, this.dataRequest)
                    .then(() => {
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
    .validation-buttons {
        margin-top: 50px;
        margin-right: 5px;
    }
</style>


