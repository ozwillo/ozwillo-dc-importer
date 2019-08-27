package org.ozwillo.dcimporter.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class WebSecurityConfig {

    companion object {
        private val logger = LoggerFactory.getLogger(WebSecurityConfig::class.java)
    }

    @Value("\${security.basicAuth.connexion.user}")
    private val user = ""
    @Value("\${security.basicAuth.connexion.password}")
    private val password = ""
    @Value("\${security.basicAuth.connexion.role}")
    private val role = ""

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {

        return http

            .authorizeExchange().pathMatchers("/api/**", "/dc/**").permitAll()
            .and()
            .authorizeExchange().anyExchange().authenticated()
            .and()
            .httpBasic()
            .and()
            .csrf().disable()
            .build()
    }

    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService {
        if (password == "changeme")
            logger.warn("Basic auth has not been set up, please review it (security.basicAuth.connexion.password) !")

        val user: UserDetails = User
            .withUsername(user)
            .password(passwordEncoder().encode(password))
            .roles(role)
            .build()
        return MapReactiveUserDetailsService(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}