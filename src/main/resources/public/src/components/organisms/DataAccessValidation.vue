<template>
    <table>
        <tr>
            <td>
                <labelled-input
                    label-class="data-access-validation-label"
                    label-value="DC Model: "
                    input-type="text"
                    v-bind:inputValue="inputValue"
                    disabled="true"
                ></labelled-input>
            </td>
            <td>
                <label id="check-all-dataset" class="data-access-validation-label">Authorize access to all "{{ inputValue }}" dataset</label>
                <input type="checkbox" v-model="authAllChecked" v-on:change="checkAll"/>
            </td>
        </tr>
        <tr id="proof-of-concept-that-will-be-removed">    <!-- TODO: Remove tr +span -->
            <span>{{ message }}</span>
        </tr>
    </table>
</template>

<script>
import LabelledInput from '../molecules/LabelledInput'
export default {
    name: 'DataAccessValidation',
    components: {
        LabelledInput
    },
    data () {
        return {
            inputValue: '',
            authAllChecked: null,
            message: ''
        }
    },
    methods: {
        checkAll: function(){
            if(this.authAllChecked)
                this.message = 'Access to all ' + this.inputValue + ' dataset will be granted'
            else
                this.message = 'Access to all ' + this.inputValue + ' dataset will be refused'
            
            //TODO: When screen completed remove tr and check/uncheck all dataset checkboxes accordingly
        }
    },
    created() {
        this.inputValue = "Model name"   //TODO: Add GET request API with mongo document id in url parameters
        this.authAllChecked= true
    }
}
</script>

<style scoped>
#check-all-dataset{
    margin-left: 200px;
}
</style>

