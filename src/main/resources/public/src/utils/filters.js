import Vue from 'vue'
import distanceInWords from 'date-fns/distance_in_words'

Vue.filter('dateDistanceInWords', value => {
    return distanceInWords(new Date(value), new Date())
})
