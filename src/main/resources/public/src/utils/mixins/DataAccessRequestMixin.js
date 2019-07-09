import axios from 'axios'

const dataAccessRequestMixin = {
    data() {
        return {
            dataRequest: {},
            errors: []
        }
    },
    methods: {
        fetchDataAccessRequest(id) {
            axios.get(`/api/data-access/${id}`)
                .then(response => {
                    this.dataRequest = response.data
                })
                .catch(e => {
                    this.errors.push(e)
                })
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

export default dataAccessRequestMixin