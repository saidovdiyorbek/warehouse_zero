package dasturlash.warehouse_zero

import dasturlash.warehouse_zero.security.JwtService
import io.jsonwebtoken.io.IOException
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.source.ConfigurationPropertySources.attach
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Calendar
import java.util.Objects.hash
import kotlin.io.path.Path

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

//Attach Service
interface AttachService {
    fun upload(productId: Long, file: MultipartFile): AttachUrl
    fun generateDataBaseFolder(): String
    fun getExtension(fileName: String?): String
    fun createAttachEntity(file: MultipartFile, hash: String, extension: String, pathFolder: String, product: Product): Attach
    fun generateHash(): String
    fun openUrl(hash: String): String
    fun isExist(hash: String): Boolean
    fun saveAttach(file: MultipartFile, pathFolder: String, hash: String, extension: String): String
}

@Service
class AttachServiceImpl(
    @Value("\${attach.upload.folder}")private val folderName: String,
    @Value("\${attach.url}")private val attachUrl: String,

    private val productRepository: ProductRepository,
    private val repository: AttachRepository
) : AttachService {

    @Transactional
    override fun upload(productId: Long, file: MultipartFile): AttachUrl {
        val findProduct = productRepository.findByIdAndDeletedFalse(productId)
            ?: throw ProductNotFoundException()

        val pathFolder: String = generateDataBaseFolder()
        val extension: String = getExtension(file.originalFilename)
        val hash: String = generateHash()
        val fullFilePath = saveAttach(file, pathFolder, hash, extension)

        val attach = createAttachEntity(file, hash, extension, pathFolder, findProduct!!)

        return AttachUrl(attach.id!!, openUrl(hash) )
    }



     override fun generateDataBaseFolder(): String {
        val cal: Calendar = Calendar.getInstance()
        val folder: String = "${cal.get(Calendar.YEAR)}/" +
                "${cal.get(Calendar.MONTH) + 1}/" +
                "${cal.get(Calendar.DATE)}"
        return folder
    }

     override fun getExtension(fileName: String?): String {
        val lastIndex = fileName?.lastIndexOf(".")
        return fileName!!.substring(lastIndex!!.plus(1))
    }

    override fun createAttachEntity(
        file: MultipartFile,
        hash: String,
        extension: String,
        pathFolder: String,
        product: Product
    ): Attach {
        val attach = Attach(
            originName = file.originalFilename,
            size = file.size,
            type = file.contentType,
            path = pathFolder,
            hash = hash,
            product = product,
        )

        return attach
    }

    override fun generateHash(): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString =  (1..10)
            .map {i -> kotlin.random.Random.nextInt(0, charPool.size).let { charPool[it] }}
            .joinToString("")
        return randomString
    }

    override fun openUrl(hash: String): String {
          if (isExist(hash)){
              return "$attachUrl/open/$hash"
          }
        return " "
    }

    override fun isExist(hash: String): Boolean {
        return repository.existsByHashAndDeletedFalse(hash)
    }

    override fun saveAttach(
        file: MultipartFile,
        pathFolder: String,
        hash: String,
        extension: String
    ): String {
        val path = Path(folderName)
        try {
            if (!Files.exists(path)){
                Files.createDirectories(path)
            }

            val fullFileName = "$hash.$extension"
            val fullPath: Path = Paths.get("$folderName/$pathFolder/$fullFileName")
            Files.createDirectories(fullPath.parent)
            Files.write(fullPath, file.bytes)

            return fullPath.toString()

        }catch (e: IOException){
            throw RuntimeException("Failed to create file", e)
        }
    }

}
//Attach Service