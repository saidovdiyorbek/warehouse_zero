package dasturlash.warehouse_zero

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.Date

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
class WarehouseController(
    private val service: WarehouseService
) {

    @Operation(summary = "Create a Warehouse")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody create: CreateWarehouseDto) = service.create(create)

    @Operation(summary = "Get one by id")
    @PreAuthorize("hasRole('ADMIN')")
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

@RestController
@RequestMapping("/api/measurements")
class MeasurementController(
    private val service: MeasurementService
){

    @Operation(summary = "Create measurement")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody create: MeasurementCreateDto) = service.create(create)

    @Operation(summary = "Get one by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): MeasurementResponse = service.getOne(id)

    @Operation(summary = "Update by id")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(@Valid @RequestBody update: MeasurementUpdateRequest,
               @PathVariable id: Long) = service.update(id, update)

    @Operation(summary = "Delete by id")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

}

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val service: ProductService
){
    @Operation(summary = "Create product")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody create: ProductCreateDto) = service.create(create)

    @Operation(summary = "Get one by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): ProductResponse = service.getOne(id)

    @Operation(summary = "Update by id")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(@Valid @RequestBody update: ProductUpdateRequest,
               @PathVariable id: Long) = service.update(id, update)

    @Operation(summary = "Delete by id")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}

@RestController
@RequestMapping("/api/attaches")
class AttachController(
    private val service: AttachService
){

    @Operation(summary = "Upload attach")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload/{productId}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@PathVariable productId: Long,
        @RequestParam("file") file: MultipartFile): AttachUrl =
        service.upload(productId,file)


    @Operation(summary = "Get open api")
    @GetMapping("/open/{photoHash}")
    fun getAttachByHash(@PathVariable photoHash: String): AttachResponse = service.getAttachByHash(photoHash)

    @Operation(summary = "Get product attaches")
    @GetMapping("/product-attaches/{productId}")
    fun getProductAttaches(@PathVariable productId: Long): List<AttachResponse> =
        service.getProductAttaches(productId)

    @Operation(summary = "Delete by hash")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{photoHash}")
    fun delete(@PathVariable photoHash: String) = service.delete(photoHash)
}

@RestController
@RequestMapping("/api/employee")
class EmployeeController(
    private val service: EmployeeService
){

    @Operation(summary = "Create employee")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody create: EmployeeCreateDto) = service.create(create)

    @Operation(summary = "Get one by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): EmployeeResponse = service.getOne(id)

    @Operation(summary = "Update by id")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(@Valid @RequestBody update: EmployeeUpdateRequest,
               @PathVariable id: Long) = service.update(id, update)

    @Operation(summary = "Delete by id")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)


    @Operation(summary = "Update employee by hash")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-status/{hash}")
    fun updateStatus(@PathVariable hash: String) = service.updateStatus(hash)
}

@RestController
@RequestMapping("/api/suppliers")
class SupplierController(
    private val service: SupplierService
){
    @Operation(summary = "Create supplier")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody create: SupplierCreateDto) = service.create(create)

    @Operation(summary = "Get one by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): SupplierResponse = service.getOne(id)

    @Operation(summary = "Update by id")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(@Valid @RequestBody update: SupplierUpdateRequest,
               @PathVariable id: Long) = service.update(id, update)

    @Operation(summary = "Delete by id")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}

@RestController
@RequestMapping("/api/stock-in")
class StockInController(
    private val service: StockInService,
){
    @Operation(summary = "Create stock-in")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody create: StockInCreateDto) = service.create(create)

    @Operation(summary = "Get daily in product information")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-daily-in-product-information")
    fun getDailyInProductInformation(
        @RequestParam
        //@DateTimeFormat(pattern = "yyyy-MM-dd")
        date: LocalDate): List<DailyInProductsResponse>? = service.getDailyInProductInformation(date)



}

@RestController
@RequestMapping("/api/stock-out")
class StockOutController(
    private val service: StockOutService,
){

    @Operation(summary = "Create stock-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    fun create(@Valid @RequestBody create: StockOutCreateDto) = service.create(create)

}

@RestController
@RequestMapping("/api/currencies")
class CurrencyController(
    private val service: CurrencyService
){
    @Operation(summary = "Create currency")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@Valid @RequestBody create: CurrencyCreateDto) = service.create(create)

    @Operation(summary = "Get one by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): CurrencyResponse = service.getOne(id)

    @Operation(summary = "Update by id")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(@Valid @RequestBody update: CurrencyUpdateRequest,
               @PathVariable id: Long) = service.update(id, update)

    @Operation(summary = "Delete by id")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}