package dasturlash.warehouse_zero

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.math.BigDecimal

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

data class CreateWarehouseDto(
    @field:NotBlank("name.not.blank")
    @field:Size(min = 4, max = 100, message = "min4.max100")
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

data class LoginRequest(
    @field:NotBlank(message = "Phone number is required")
    @field:Pattern(
        regexp = "^\\+998(90|91|93|94|95|97|98|99|33|50|88)\\d{7}$",
        message = "phone.number.regex"
    )
    val phoneNumber: String,
    @field:NotBlank("password.required") val password: String,
)

data class JwtResponse(val token: String)

data class WarehouseResponse(
    val id: Long,
    val name: String,
    val createdBy: String?,
)

data class WarehouseUpdateRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 100, message = "warehouse.update.name.min.max")
    val name: String
)

data class CreateCategoryDto(
    @field:NotBlank(message = "category.name.required")
    @field:Size(min = 4, max = 100, message = "category.name.min.max")
    val name: String,
    val parentId: Long?
)

data class CategoryResponse(
    val id: Long,
    val name: String,
    val createdBy: String?,
    val parentId: Long?,
)

data class CategoryUpdateRequest(
    @field:Size(min = 4, max = 100, message = "category.name.min.max")
    val name: String?,
    val parentId: Long?,
)

data class AttachUrl(
    val hash: String,
    val url: String,
)

data class MeasurementCreateDto(
    @field:NotBlank(message = "measurement.name.required")
    @field:Size(min = 2, max = 100, message = "measurement.name.min.max")
    val name: String
)

data class MeasurementResponse(
    val id: Long,
    val name: String,
    val createdBy: String?,
)

data class MeasurementUpdateRequest(
    @field:NotBlank(message = "measurement.name.required")
    @field:Size(min = 2, max = 100, message = "measurement.name.min.max")
    val name: String,
)

data class ProductCreateDto(
    val name: String,
    val categoryId: Long,
    val measurementId: Long,
)

data class ProductResponse(
    val id: Long,
    val name: String,
    val categoryId: Long,
    val productNumber: Int,
    val measurementId: Long,
    val inPrice: BigDecimal?,
    val outPrice: BigDecimal?,
)

data class ProductUpdateRequest(
    val name: String,
    val categoryId: Long,
    val measurementId: Long,
)

data class AttachResponse(
    val id: Long,
    val originName: String?,
    val size: Long,
    val type: String?,
    val path: String,
    val fullPath: String,
    val hash: String,
    val productId: Long
)