package br.com.jamadeu.multicontas.configuration

import org.springframework.boot.autoconfigure.web.WebProperties.Resources
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Configuration {

    @Bean
    fun resources(): Resources = Resources()
}