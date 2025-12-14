package dasturlash.warehouse_zero

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

data class CreateWarehouseDto(
    @field:NotBlank("Warehouse name not be blank")
    @field:Size(min = 4, max = 100, message = "Warehouse name size between 4 and 100 characters.")
    var name: String,
)

data class UserDetailsResponse(
    val id: Long,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String?,
    val role: Role,
    val mypassword: String,
) : UserDetails{
    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return listOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String {
        return mypassword
    }

    override fun getUsername(): String {
        return phoneNumber
    }

}