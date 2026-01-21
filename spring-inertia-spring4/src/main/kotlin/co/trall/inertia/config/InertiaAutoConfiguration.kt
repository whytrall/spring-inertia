package co.trall.inertia.config

import co.trall.inertia.Inertia
import co.trall.inertia.InertiaResponseFactory
import co.trall.inertia.http.InertiaInterceptor
import co.trall.inertia.http.InertiaResponseHandler
import co.trall.inertia.ssr.HttpSsrGateway
import co.trall.inertia.ssr.SsrGateway
import co.trall.inertia.thymeleaf.InertiaDialect
import tools.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestClient
import org.thymeleaf.spring6.SpringTemplateEngine

/**
 * Auto-configuration for the Inertia library.
 *
 * This configuration is automatically applied when the library is on the classpath
 * and the application is a web application.
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(InertiaProperties::class)
@Import(InertiaWebMvcConfigurer::class)
class InertiaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun inertiaResponseFactory(
        properties: InertiaProperties,
        objectMapper: ObjectMapper
    ): InertiaResponseFactory {
        return InertiaResponseFactory(properties, objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean
    fun inertia(factory: InertiaResponseFactory): Inertia {
        return Inertia(factory)
    }

    @Bean
    @ConditionalOnMissingBean(InertiaInterceptor::class)
    fun inertiaInterceptor(properties: InertiaProperties): InertiaInterceptor {
        return InertiaInterceptor(properties)
    }

    @Bean
    @ConditionalOnMissingBean
    fun inertiaResponseHandler(objectMapper: ObjectMapper): InertiaResponseHandler {
        return InertiaResponseHandler(objectMapper)
    }

    /**
     * Thymeleaf integration configuration.
     */
    @Configuration
    @ConditionalOnClass(SpringTemplateEngine::class)
    class ThymeleafConfiguration {

        @Bean
        @ConditionalOnMissingBean
        fun inertiaDialect(): InertiaDialect {
            return InertiaDialect()
        }
    }

    /**
     * SSR configuration.
     */
    @Configuration
    @ConditionalOnProperty(prefix = "inertia.ssr", name = ["enabled"], havingValue = "true")
    class SsrConfiguration {

        @Bean
        @ConditionalOnMissingBean(SsrGateway::class)
        fun ssrGateway(
            properties: InertiaProperties,
            restClientBuilder: RestClient.Builder
        ): SsrGateway {
            return HttpSsrGateway(properties, restClientBuilder)
        }
    }
}
