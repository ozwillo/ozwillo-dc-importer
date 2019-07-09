import Vue from 'vue'
import distanceInWords from 'date-fns/distance_in_words'
import format from 'date-fns/format'
import fr from 'date-fns/locale/fr'

Vue.filter('dateDistanceInWords', value => {
    return distanceInWords(new Date(value), new Date(), {locale: fr})
})

Vue.filter('formatDate', value => {
    return format(value, 'ddd D MMM YYYY', {locale: fr})
})
