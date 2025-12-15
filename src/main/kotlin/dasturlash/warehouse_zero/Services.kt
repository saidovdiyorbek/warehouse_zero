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