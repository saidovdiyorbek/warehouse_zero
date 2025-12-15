package dasturlash.warehouse_zero

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val employeeService: EmployeeService
) {

    @Operation(summary = "Login employee")
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): JwtResponse {
        return employeeService.login(request)
    }
}

@RestController
@RequestMapping("/api/warehouses")
@SecurityRequirement(name = "Bearer Authentication")
class WarehouseController(
    private val service: WarehouseService
) {

    @Operation(summary = "Create a Warehouse")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody create: CreateWarehouseDto) = service.create(create)

    @Operation(summary = "Get one by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): WarehouseResponse = service.getOne(id)

    @Operation(summary = "Update by id")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(@Valid @RequestBody update: WarehouseUpdateRequest,
               @PathVariable id: Long) = service.update(id, update)

    @Operation(summary = "Delete by id")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val service: CategoryService
){

    @Operation(summary = "Create a Category")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody create: CreateCategoryDto) = service.create(create)

    @Operation(summary = "Get one by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): CategoryResponse = service.getOne(id)

    @Operation(summary = "Update by id")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(@Valid @RequestBody update: CategoryUpdateRequest,
               @PathVariable id: Long) = service.update(id, update)

    @Operation(summary = "Delete by id")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

}
