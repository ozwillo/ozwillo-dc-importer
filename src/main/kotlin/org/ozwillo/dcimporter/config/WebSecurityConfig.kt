package org.ozwillo.dcimporter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@EnableWebFluxSecurity
class WebSecurityConfig{

    @Bean
    fun securitygWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain{

        return http

        .authorizeExchange().pathMatchers( "/dc/**", "/api/status/**", "/api/publik/**", "/api/maarch/**", "/api/marche-public/**", "/a/token").permitAll()
                .and()
                .authorizeExchange().anyExchange().authenticated()
                .and()
                .httpBasic()
                .and()
                .csrf().disable()
                .build()
    }

    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService{
        val user: UserDetails = User
                .withUsername("sictiam")
                .password(passwordEncoder().encode("password"))
                .roles("DEV")
                .build()
        return MapReactiveUserDetailsService(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

}