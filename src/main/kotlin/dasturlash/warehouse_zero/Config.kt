package dasturlash.warehouse_zero

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class MyConfiguration{
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}