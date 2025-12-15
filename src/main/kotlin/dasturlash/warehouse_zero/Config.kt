package dasturlash.warehouse_zero

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils
import java.util.Locale
import java.util.Optional
import org.springframework.web.method.HandlerMethod
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme

@Configuration
class WebMvcConfig : WebMvcConfigurer {

    @Bean
    fun localResolver() = SessionLocaleResolver().apply {
        setDefaultLocale(Locale("uz"))
    }

    @Bean fun messageSource(): MessageSource {
        val source = ResourceBundleMessageSource()
        source.setBasename("error")
        source.setDefaultEncoding("UTF-8")
        source.setFallbackToSystemLocale(false)
        return source
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(object : AsyncHandlerInterceptor {
            override fun preHandle(
                request: HttpServletRequest,
                response: HttpServletResponse,
                handler: Any
            ): Boolean {
                request.getHeader("hl")?.let {
                    RequestContextUtils.getLocaleResolver(request)
                        ?.setLocale(request, response, Locale(it))
                }
                return true
            }
        })
    }
}

@Configuration
class MyConfiguration{
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}

@Configuration
class AuditConfig{

    @Bean
    fun auditorAware(): AuditorAware<String> {
        return AuditorAware {
            Optional.ofNullable(
                SecurityContextHolder.getContext().authentication.name ?: "system"
            )
        }
    }
}

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList("bearerAuth")
            )
    }

    @Bean
    fun globalHeaderCustomizer() : OperationCustomizer{
        return OperationCustomizer {operation, _ ->

            val localeHeader = Parameter()
                .`in`("header")
                .schema(StringSchema())
                .name("Accept-Language")
                .description("Language (uz, ru, en)")
                .required(false)

            operation.addParametersItem(localeHeader)
            operation
        }
    }

}