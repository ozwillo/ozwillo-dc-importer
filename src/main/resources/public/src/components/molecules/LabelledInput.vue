<template>
    <div v-bind:id="labelledInputId" v-bind:class="labelledInputClass">
        <label v-bind:class="labelClass">{{ labelValue }}</label>
        <input 
            v-bind:id="inputId"
            v-bind:class="inputClass"
            v-bind:type="inputType"
            v-bind:value="inputValue"
            v-bind:disabled="disabled"
            v-bind:checked="checked"
            v-on:change="$emit('change', $event.target.value)"
        />
    </div>
</template>

<script>
import Input from '../atoms/Input'
export default {
    name: 'LabelledInput',
    components: {
        Input
    },
    model: {
        prop: 'inputValue',
        event: 'change'
    },
    props: [
        'labelledInputId',
        'labelledInputClass',
        'labelClass',
        'labelValue',
        'inputId',
        'inputClass',
        'inputType',
        'inputValue',
        'disabled',
        'checked'
    ]
}
</script>

<style scoped>

</style>


