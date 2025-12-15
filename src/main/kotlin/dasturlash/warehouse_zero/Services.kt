package dasturlash.warehouse_zero

import dasturlash.warehouse_zero.security.JwtService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

//Warehouse Service
interface WarehouseService {
    fun create(create: CreateWarehouseDto)
    fun getOne(id: Long): WarehouseResponse
    fun update(id: Long, update: WarehouseUpdateRequest)
    fun delete(id: Long)
}

@Service
class WarehouseServiceImpl(
    private val repository: WarehouseRepository
) : WarehouseService {
    override fun create(create: CreateWarehouseDto) {
        repository.existsWarehouseByName(create.name)?.let {
            throw WarehouseAlreadyExistsException()
        }
        repository.save(Warehouse(name = create.name,))
    }

    override fun getOne(id: Long): WarehouseResponse {
        val warehouse = repository.findByIdAndDeletedFalse(id)?.let { warehouse ->
            return WarehouseResponse(
                warehouse.id!!,
                warehouse.name,
                warehouse.createdBy
            )
        }
        throw WarehouseNotFoundException()
    }

    override fun update(id: Long, update: WarehouseUpdateRequest) {
        repository.findByIdAndDeletedFalse(id)?.let { warehouse ->
            repository.existsWarehouseByName(update.name)?.let {
                throw WarehouseAlreadyExistsException()
            }
            warehouse.name = update.name
            repository.save(warehouse)
            return
        }
        throw WarehouseNotFoundException()
    }

    override fun delete(id: Long) {
        repository.trash(id) ?: throw WarehouseNotFoundException()
    }
}
//Warehouse Service

//Custom User detail service
@Service
class CustomUserService(
    private val repository: EmployeeRepository
) : UserDetailsService {
    override fun loadUserByUsername(phoneNumber: String): UserDetails {
        return repository.findByPhoneNumberAndDeletedFalse(phoneNumber)?.let {
            UserDetailsResponse(
                id = it.id!!,
                phoneNumber = it.phoneNumber,
                firstName = it.firstName,
                lastName = it.lastName,
                role = it.role,
                mypassword = it.password
            )
        } ?: throw EmployeeNotFoundException()
    }
}
//Custom User detail service

//Employee Service
interface EmployeeService {
    fun login(request: LoginRequest): JwtResponse
}

@Service
class EmployeeServiceImpl(
    private val repository: EmployeeRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) : EmployeeService {
    override fun login(request: LoginRequest): JwtResponse {
       val user = repository.findByPhoneNumberAndDeletedFalse(request.phoneNumber)
        ?: throw EmployeeNotFoundException()

       if (!passwordEncoder.matches(request.password, user.password))
           throw InvalidPasswordException()

       val token = jwtService.generateToken(user.phoneNumber, user.role.name)

       return JwtResponse(token)
    }
}
//Employee Service

//Category Service
interface CategoryService {
    fun create(create: CreateCategoryDto)
    fun getOne(id: Long): CategoryResponse
    fun update(id: Long, update: CategoryUpdateRequest)
    fun delete(id: Long)
}

@Service
class CategoryServiceImpl(
    private val repository: CategoryRepository,
) : CategoryService {
    override fun create(create: CreateCategoryDto) {
        repository.existsCategoryByNameAndDeletedFalse(create.name).takeIf { it == true}?.let {
            throw CategoryAlreadyExistsException()
        }
        val category = create.parentId?.let {
            repository.findByIdAndDeletedFalse(create.parentId)
            throw CategoryNotFoundException()
        }

        repository.save(Category(
            name = create.name,
            parent = category,
        ))
    }

    override fun getOne(id: Long): CategoryResponse {
        val category = repository.findByIdAndDeletedFalse(id)?.let { category ->
            return CategoryResponse(
                category.id!!,
                category.name,
                category.createdBy,
                category.parent?.id
            )
        }
        throw WarehouseNotFoundException()
    }

    override fun update(id: Long, update: CategoryUpdateRequest) {
        repository.findByIdAndDeletedFalse(id)?.let { category ->
                repository.existsCategoryByNameAndDeletedFalse(category.name).takeIf { it == true}?.let {
                    category.name = update.name.toString()
                    if (update.parentId != category.id) category.parent = update.parentId as Category?
                    repository.save(category)
                    return
            }
            throw CategoryAlreadyExistsException()
        }
        throw CategoryNotFoundException()
    }

    override fun delete(id: Long) {
        repository.trash(id) ?: throw CategoryNotFoundException()
    }

}
//Category Service