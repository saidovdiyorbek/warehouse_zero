package dasturlash.warehouse_zero

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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