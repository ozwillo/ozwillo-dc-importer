import Vue from 'vue'
import distanceInWords from 'date-fns/distance_in_words'
import format from 'date-fns/format'

Vue.filter('dateDistanceInWords', value => {
    return distanceInWords(new Date(value), new Date())
})

Vue.filter('formatDate', value => {
    return format(value, 'ddd[,] MMM Do YYYY')
})