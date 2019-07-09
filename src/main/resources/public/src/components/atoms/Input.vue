<template>
    <input 
        v-bind:id="inputId"
        v-bind:class="inputClass"
        v-bind:type="inputType"
        v-bind:value="inputValue"
        v-bind:disabled="disabled"
        v-bind:checked="checked"
        v-on:change="$emit('change', $event.target.value)"
    />
</template>

<script>
export default {
    name: 'Input',
    model: {
        prop: 'inputValue',
        event: 'change'
    },
    props: [
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


