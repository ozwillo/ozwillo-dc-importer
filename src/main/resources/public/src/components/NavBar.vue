<template>
    <nav id="navbar" class="navbar navbar-expand-lg navbar-light">
        <router-link to="/" class="navbar-brand">
            <span id="app-name" class="ml-sm-1">DC Importer</span>
        </router-link>
        <button
            class="navbar-toggler"
            data-toggle="collapse"
            data-target="#navbarSupportedContent"
            aria-expanded="false"
            aria-label="Toggle navigation"
            aria-controls="navbarSupportedContent">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav mr-auto">
                <li class="nav-item active">
                    <router-link class="nav-link nav-link-title" to="/">
                        {{ $t('dashboard') }}
                        <span class="sr-only">(current)</span>
                    </router-link>
                </li>
                <li class="nav-item">
                    <router-link class="nav-link nav-link-title" to="/request">
                      {{ $t('data_request') }}
                      <span class="sr-only">(current)</span>
                    </router-link>
                </li>
                <li class="nav-item">
                    <router-link class="nav-link nav-link-title" to="/connectors-management">
                      {{ $t('connectors_management') }}
                      <span class="sr-only">(current)</span>
                    </router-link>
                </li>
                <li class="nav-item">
                    <router-link class="nav-link nav-link-title" to="/stat-view">
                      {{ $t('statistics') }}
                      <span class="sr-only">(current)</span>
                    </router-link>
                </li>
            </ul>
        </div>
    </nav>
</template>

<script>
    import Vue from 'vue'
    import VueRouter from 'vue-router'
    import Button from './atoms/Button.vue'

    export default {
        name: "NavBar",
        components: {Button}
    }
</script>

<style scoped  lang="scss">
  #navbar {
    background-color: #FBFBFB
  }
  #app-name {
    color: #2C55A2;
    text-transform: uppercase;
    font-weight: bold;
  }
  .nav-link-title {
      color: #4c4c4c;
      text-transform: uppercase;
  }
</style>
