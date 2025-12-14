package dasturlash.warehouse_zero

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/warehouses")
class WarehouseController(
    private val service: WarehouseService
) {

    @Operation(summary = "Create a Warehouse")
    @PostMapping
    fun create(@Valid @RequestBody create: CreateWarehouseDto) = service.create(create)
}