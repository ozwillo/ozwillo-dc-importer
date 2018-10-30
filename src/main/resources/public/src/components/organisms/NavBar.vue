<template>
  <nav id="navbar" class="navbar navbar-expand-lg navbar-light">
    <NavRouterLink linkedTo="/" className="navbar-brand" routerLinkName="DC Importer" childId="app-name" childClass="ml-sm-1">
      <Logo width="30" height="30" className="d-inline-block align-top" altName="logo_ozwillo" />
    </NavRouterLink>
    <Button
            className="navbar-toggler"
            dataToggle="collapse"
            dataTarget="#navbarSupportedContent"
            ariaExpanded="false"
            ariaLabel="Toggle navigation"
            ariaControls="navbarSupportedContent">
      <span class="navbar-toggler-icon"></span>
    </Button>
    <div class="collapse navbar-collapse" id="navbarSupportedContent">
      <ul class="navbar-nav mr-auto">
        <li class="nav-item active">
          <router-link class="nav-link" to="/" style="color:#fff">
            dashboard
            <span class="sr-only">(current)</span>
          </router-link>
        </li>
        <li class="nav-item active">
          <router-link class="nav-link" to="/create" style="color:#fff">
            Data access request
            <span class="sr-only">(current)</span>
          </router-link>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script>
    import Vue from 'vue'
    import  VueRouter from 'vue-router'
    import Button from '../atoms/Button'
    import Logo from '../atoms/Logo'
    import NavRouterLink from '../molecules/NavRouterLink'

    export default {
        name: "NavBar",
        components: {
          Button,
          Logo,
          NavRouterLink
          }
    }
</script>

<style scoped  lang="scss">
  #navbar {
    background-color: #4c2d62
  }
</style>
