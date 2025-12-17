package dasturlash.warehouse_zero

import dasturlash.warehouse_zero.security.JwtService
import dasturlash.warehouse_zero.security.SecurityUtils
import io.jsonwebtoken.io.IOException
import jakarta.transaction.Transactional
import jakarta.ws.rs.ForbiddenException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import kotlin.io.path.Path
import kotlin.toString

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
    fun create(create: EmployeeCreateDto)
    fun getOne(id: Long): EmployeeResponse
    fun update(id: Long, update: EmployeeUpdateRequest)
    fun delete(id: Long)
}

@Service
class EmployeeServiceImpl(
    private val repository: EmployeeRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val genHash: GenerateHash,
    private val wareRepository: WarehouseRepository,
) : EmployeeService {
    override fun login(request: LoginRequest): JwtResponse {
       val user = repository.findByPhoneNumberAndDeletedFalse(request.phoneNumber)
        ?: throw EmployeeNotFoundException()

       if (!passwordEncoder.matches(request.password, user.password))
           throw InvalidPasswordException()

       val token = jwtService.generateToken(user.phoneNumber, user.role.name)

       return JwtResponse(token)
    }

    override fun create(create: EmployeeCreateDto) {
        repository.findByPhoneNumber(create.phoneNumber)?.let {
            throw EmployeeAlreadyExistsException()
        }
        create.warehouseId?.let {
            wareRepository.findByIdAndDeletedFalse(create.warehouseId)?.let { warehouse ->
                repository.save(Employee(
                    create.firstName,
                    create.lastName,
                    create.phoneNumber,
                    genHash.generateHash(),
                    passwordEncoder.encode(create.password),
                    warehouse,
                    Role.ROLE_EMPLOYEE
                ))
                return
            }
            throw WarehouseNotFoundException()
        }
        repository.save(Employee(
            create.firstName,
            create.lastName,
            create.phoneNumber,
            genHash.generateHash(),
            passwordEncoder.encode(create.password),
            null,
            Role.ROLE_EMPLOYEE
        ))

    }

    override fun getOne(id: Long): EmployeeResponse {
        repository.findByIdAndDeletedFalse(id)?.let { emp ->
            return EmployeeResponse(
                emp.id!!,
                emp.firstName,
                emp.lastName,
                emp.phoneNumber,
                emp.warehouse?.id,
                emp.role
            )
        }
        throw EmployeeNotFoundException()
    }

    override fun update(id: Long, update: EmployeeUpdateRequest) {
        repository.findByIdAndDeletedFalse(id)?.let { emp ->
            update.run {
                update.firstName?.let { emp.firstName = firstName!! }
                update.lastName?.let { emp.lastName = lastName!! }
                update.phoneNumber?.let {
                    repository.findByPhoneNumber(update.phoneNumber)?.let {
                        throw EmployeeAlreadyExistsException()
                    }
                    emp.phoneNumber = phoneNumber!! }
                update.warehouseId?.let {
                    wareRepository.findByIdAndDeletedFalse(it)?.let {ware -> emp.warehouse = ware}
                    throw WarehouseNotFoundException()
                }
            }
            repository.save(emp)
            return
        }
    }

    override fun delete(id: Long) {
        repository.trash(id) ?: throw EmployeeNotFoundException()
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
                    throw CategoryAlreadyExistsException()
            }
            category.name = update.name.toString()
            if (update.parentId != category.id) category.parent = update.parentId as Category?
            repository.save(category)
            return
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
    fun createAttachEntity(file: MultipartFile, hash: String, extension: String, pathFolder: String, product: Product, fullPath: String): Attach
    fun openUrl(hash: String): String
    fun isExist(hash: String): Boolean
    fun saveAttach(file: MultipartFile, pathFolder: String, hash: String, extension: String): String
    fun getAttachByHash(photoHash: String): AttachResponse
    fun delete(photoHash: String)
    fun deleteFileFromFolder(folder: String, fileName: String): Boolean
    fun getProductAttaches(productId: Long): List<AttachResponse>
}

@Service
class AttachServiceImpl(
    @Value("\${attach.upload.folder}")private val folderName: String,
    @Value("\${attach.url}")private val attachUrl: String,

    private val productRepository: ProductRepository,
    private val repository: AttachRepository,
    private val genHash: GenerateHash
) : AttachService {

    @Transactional
    override fun upload(productId: Long, file: MultipartFile): AttachUrl {
        val findProduct = productRepository.findByIdAndDeletedFalse(productId)
            ?: throw ProductNotFoundException()

        val pathFolder: String = generateDataBaseFolder()
        val extension: String = getExtension(file.originalFilename)
        val hash: String = genHash.generateHash()
        val fullFilePath = saveAttach(file, pathFolder, hash, extension)

        val attach = createAttachEntity(file, hash, extension, pathFolder, findProduct!!, fullFilePath)

        return AttachUrl(attach.hash, openUrl(hash) )
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
        product: Product,
        fullPath: String
    ): Attach {
        val attach = repository.save(Attach(
            originName = file.originalFilename,
            size = file.size,
            type = file.contentType,
            path = "$folderName/$pathFolder",
            hash = hash,
            product = product,
            fullPath = fullPath,
        ))

        return attach
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
            var fullFileName: String
            if (file.originalFilename.isNullOrBlank()){
                fullFileName = "$hash.$extension"
            }
            fullFileName = file.originalFilename!!
            val fullPath: Path = Paths.get("$folderName/$pathFolder/$fullFileName")
            Files.createDirectories(fullPath.parent)
            Files.write(fullPath, file.bytes)

            return fullPath.toString()

        }catch (e: IOException){
            throw RuntimeException("Failed to create file", e)
        }
    }

    override fun getAttachByHash(photoHash: String): AttachResponse {
        repository.findAttachByHashAndDeletedFalse(photoHash)?.let { attach ->
            return AttachResponse(
                id = attach.id!!,
                originName = attach.originName,
                size = attach.size,
                type = attach.type,
                path = attach.path,
                fullPath = attach.fullPath,
                hash = attach.hash,
                productId = attach.product.id!!
            )
        }
        throw AttachNotFoundException()
    }

    override fun delete(photoHash: String) {
        repository.findAttachByHashAndDeletedFalse(photoHash)?.let { attach ->
            repository.trash(attach.id!!)
            deleteFileFromFolder(attach.path, attach.originName!!)
        }
    }

    override fun deleteFileFromFolder(folder: String, fileName: String): Boolean{
        return try {
            val filePath = Paths.get(folder, fileName)

            Files.deleteIfExists(filePath)
        }catch (e: Exception){
            println("Problem delete file ${e.message}")
            false
        }
    }

    override fun getProductAttaches(productId: Long): List<AttachResponse> {
        val response: MutableList<AttachResponse> = mutableListOf()
        productRepository.findByIdAndDeletedFalse(productId)?.let { product ->
            repository.findAllByProductIdAndDeletedFalse(productId).map { attach ->
                response.add(AttachResponse(
                    id = attach.id!!,
                    originName = attach.originName,
                    size = attach.size,
                    type = attach.type,
                    path = attach.path,
                    fullPath = attach.fullPath,
                    hash = attach.hash,
                    productId = attach.product.id!!
                ))
            }
            return response
        }
        throw ProductNotFoundException()
    }

}
//Attach Service

//Product Service
interface ProductService{
    fun create(create: ProductCreateDto)
    fun getOne(id: Long): ProductResponse
    fun update(id: Long, update: ProductUpdateRequest)
    fun delete(id: Long)
    fun generateUniqueCode(): Int
}

@Service
class ProductServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val measurementRepository: MeasurementRepository,
    private val repository: ProductRepository,
    private val stockInItemRepository: StockInItemRepository
) : ProductService{
    override fun create(create: ProductCreateDto) {
        val category =
            categoryRepository.findByIdAndDeletedFalse(create.categoryId) ?: throw CategoryNotFoundException()
        val measurement =
            measurementRepository.findByIdAndDeletedFalse(create.measurementId) ?: throw MeasurementNotFoundException()

            val uniqueNumber = generateUniqueCode()

            repository.existsProductByNameAndDeletedFalse(create.name).takeIf { it }
                ?.let {
                    throw ProductAlreadyExistsException()
                }
            repository.save(
                Product(
                    name = create.name,
                    category = category,
                    measurement = measurement,
                    productNumber = uniqueNumber,
                )
            )
    }

    override fun getOne(id: Long): ProductResponse {
        repository.findByIdAndDeletedFalse(id)?.let { findProduct ->
            val stockInItem = stockInItemRepository.findStockInItemByProductId(findProduct.id!!)
            return ProductResponse(
                id = findProduct.id!!,
                name = findProduct.name,
                categoryId = findProduct.category.id!!,
                productNumber = findProduct.productNumber,
                measurementId = findProduct.measurement.id!!,
                inPrice = stockInItem?.inPrice,
                outPrice = stockInItem?.outPrice,
            )
        }
        throw ProductNotFoundException()
    }

    override fun update(id: Long, update: ProductUpdateRequest) {
        repository.findByIdAndDeletedFalse(id)?.let { findProduct ->
            repository.existsProductByNameAndDeletedFalseAndIdNot(update.name, id).takeIf { it }
                ?.let {
                    throw ProductAlreadyExistsException()
                }
            val category =
                categoryRepository.findByIdAndDeletedFalse(update.categoryId) ?: throw CategoryNotFoundException()
            val measurement =
                measurementRepository.findByIdAndDeletedFalse(update.measurementId) ?: throw MeasurementNotFoundException()



            findProduct.name = update.name
            findProduct.category = category
            findProduct.measurement = measurement
            repository.save(findProduct)
        }
    }

    override fun delete(id: Long) {
        repository.trash(id) ?: throw ProductNotFoundException()
    }

    override fun generateUniqueCode(): Int {
        var uniqueNumber: Int

        while (true){
            val candidate = kotlin.random.Random.nextInt(10000, Int.MAX_VALUE)

            val alreadyExists = repository.existsProductByProductNumber(candidate)

            if (!alreadyExists){
                uniqueNumber = candidate
                break
            }

        }
        return uniqueNumber
    }
}
//Product Service

//Measurement Service
interface MeasurementService{
    fun create(create: MeasurementCreateDto)
    fun getOne(id: Long): MeasurementResponse
    fun update(id: Long, update: MeasurementUpdateRequest)
    fun delete(id: Long)
}

@Service
class MeasurementServiceImpl(
    private val repository: MeasurementRepository,
) : MeasurementService{
    override fun create(create: MeasurementCreateDto) {
        repository.existsMeasurementByNameAndDeletedFalse(create.name).takeIf { it == true }?.let {
            throw MeasurementAlreadyExistsException()
        }

        repository.save(Measurement(
            name = create.name,
        ))
    }

    override fun getOne(id: Long): MeasurementResponse {
        repository.findByIdAndDeletedFalse(id)?.let { measurement ->
            return MeasurementResponse(
                measurement.id!!,
                measurement.name,
                measurement.createdBy
            )
        }
        throw MeasurementNotFoundException()
    }

    override fun update(id: Long, update: MeasurementUpdateRequest) {
        repository.findByIdAndDeletedFalse(id)?.let { measurement ->
            repository.existsMeasurementByNameAndDeletedFalse(update.name).takeIf { it == true}?.let {
                throw MeasurementAlreadyExistsException()

            }
            measurement.name = update.name.toString()
            repository.save(measurement)
            return

        }
        throw MeasurementNotFoundException()
    }

    override fun delete(id: Long) {
        repository.trash(id) ?: throw MeasurementNotFoundException()
    }
}
//Measurement Service

//Stock in Service
interface StockInService{
    fun create(create: StockInCreateDto)
    fun getDailyInProductInformation(date: Date): List<DailyInProductsResponse>?
}

@Service
class StockInServiceImpl(
    private val warehouseRepository: WarehouseRepository,
    private val supplierRepository: SupplierRepository,
    private val currencyRepository: CurrencyRepository,
    private val repository: StockInRepository,
    private val productRepository: ProductRepository,
    private val stockInItemRepository: StockInItemRepository,
    private val warehouseBalanceRepository: WarehouseProductBalanceRepository,
    private val genHash: GenerateHash
) : StockInService{

    @Transactional
    override fun create(create: StockInCreateDto) {
        val warehouse =
            warehouseRepository.findWarehouseByIdAndActive(create.warehouseId) ?: throw WarehouseNotFoundException()
        val supplier =
            supplierRepository.findByIdAndDeletedFalse(create.supplierId) ?: throw SupplierNotFoundException()
        val currency =
            currencyRepository.findByIdAndDeletedFalse(create.currencyId) ?: throw CurrencyNotFoundException()

        var stockIn = StockIn(
            warehouse = warehouse,
            supplier = supplier,
            currency = currency,
            factualNumber = create.factualNumber,
            uniqueNumber = genHash.generateHash(),
            amount = BigDecimal.ZERO
        )

        stockIn = repository.save(stockIn)

        val items = create.items.map { itemR ->
            val product = productRepository.findByIdAndDeletedFalse(itemR.productId) ?: throw ProductNotFoundException()
            warehouseBalanceRepository.findByWarehouseIdAndProductId(warehouse.id!!, product.id!!)?.let { balance ->
                balance.quantity += itemR.measurementCount
                warehouseBalanceRepository.save(balance)
            }

            warehouseBalanceRepository.save(WarehouseProductsBalance(
                warehouse,
                product,
                itemR.measurementCount,
            ))
            StockInItem(
                stockIn = stockIn,
                product = product,
                measurementCount = itemR.measurementCount,
                inPrice = itemR.inPrice,
                outPrice = itemR.outPrice,
                expireDate = itemR.expireDate?.let { itemR.expireDate },
                notifyBeforeDay = itemR.notifyBeforeDay,
            )

        }
        stockInItemRepository.saveAll(items)

        val totalAmount = items.sumOf { it.inPrice * it.measurementCount.toBigDecimal() }
        stockIn.amount = totalAmount
        stockIn.processingStatus = StockInOutProcessingStatus.COMPLETED
        repository.save(stockIn)
    }

    override fun getDailyInProductInformation(date: Date): List<DailyInProductsResponse>? {
       val products =  repository.findDailyInProductsSum(date)?.map { product ->
            DailyInProductsResponse(
                product.getProductName(),
                product.getSum(),
                product.getMeasureCount()
            )
        }
        return products
    }
}
//Stock in Service

//StockItem
interface StockInItemService{}

@Service
class StockInItemServiceImpl() : StockInItemService{}
//StockItem

//Supplier Service
interface SupplierService{
    fun create(create: SupplierCreateDto)
    fun getOne(id: Long): SupplierResponse
    fun update(id: Long, update: SupplierUpdateRequest)
    fun delete(id: Long)
}

@Service
class SupplierServiceImpl(
    private val repository: SupplierRepository,

): SupplierService{
    override fun create(create: SupplierCreateDto) {
        repository.existsSupplierByPhoneNumber(create.phoneNumber).takeIf { it }?.let {
            throw PhoneNumberAlreadyExistsException()
        }
        repository.save(Supplier(
            create.name,
            create.phoneNumber,
        ))
    }

    override fun getOne(id: Long): SupplierResponse {
        repository.findByIdAndDeletedFalse(id)?.let { supplier ->
            return SupplierResponse(
                supplier.name,
                supplier.phoneNumber,
            )
        }
        throw SupplierNotFoundException()
    }

    override fun update(id: Long, update: SupplierUpdateRequest) {
        repository.findByIdAndDeletedFalse(id)?.let { supplier ->
            update.name?.let { supplier.name = update.name }
            update.phoneNumber?.let {
                repository.existsSupplierByPhoneNumber(update.phoneNumber).takeIf { it }?.let {
                    throw PhoneNumberAlreadyExistsException()
                }
                supplier.phoneNumber = update.phoneNumber
                repository.save(supplier)
            }
        }
    }

    override fun delete(id: Long) {
        repository.trash(id) ?: throw SupplierNotFoundException()
    }
}
//Supplier Service

//StockOut
interface StockOutService{
    fun create(create: StockOutCreateDto)
}

@Service
class StockOutServiceImpl(
    private val warehouseRepository: WarehouseRepository,
    private val currencyRepository: CurrencyRepository,
    private val employeeRepository: EmployeeRepository,
    private val repository: StockOutRepository,
    private val productRepository: ProductRepository,
    private val stockOutItemRepository: StockOutItemRepository,
    private val genHash: GenerateHash,
    private val securityUtils: SecurityUtils,
    private val warehouseBalanceRepository: WarehouseProductBalanceRepository
) : StockOutService{

    @Transactional
    override fun create(create: StockOutCreateDto) {
        val warehouse =
            warehouseRepository.findWarehouseByIdAndActive(create.warehouseId) ?: throw WarehouseNotFoundException()
        val employee =
            employeeRepository.findByIdAndDeletedFalse(securityUtils.getCurrentUserId()) ?: throw EmployeeNotFoundException()
        val currency =
            currencyRepository.findByIdAndDeletedFalse(create.currencyId) ?: throw CurrencyNotFoundException()

        if (employee.warehouse?.id != create.warehouseId) {
            throw ForbiddenException()
        }

        var stockOut = StockOut(
            warehouse = warehouse,
            employee = employee,
            currency = currency,
            factualNumber = create.factualNumber,
            uniqueNumber = genHash.generateHash(),
            amount = BigDecimal.ZERO,
        )

        stockOut = repository.save(stockOut)

        val items = create.items.map { itemR ->
            val product = productRepository.findByIdAndDeletedFalse(itemR.productId) ?: throw ProductNotFoundException()
            warehouseBalanceRepository.findByWarehouseIdAndProductId(warehouse.id!!, product.id!!)?.let { balance ->
                if (balance.quantity > itemR.measurementCount) {
                    balance.quantity -= itemR.measurementCount
                    warehouseBalanceRepository.save(balance)
                }
                throw ProductNotFoundException()
            }
            StockOutItem(
                stockOut = stockOut,
                product = product,
                measurementCount = itemR.measurementCount,
                uniqueNumber = genHash.generateHash(),
                amount = itemR.outPrice.multiply( itemR.measurementCount.toBigDecimal()),
            )
        }
        stockOutItemRepository.saveAll(items)

        val totalAmount = items.sumOf { it.amount }
        stockOut.amount = totalAmount
        stockOut.processingStatus = StockInOutProcessingStatus.COMPLETED
        repository.save(stockOut)
    }

}
//StockOut

//Currency
interface CurrencyService{
    fun create(create: CurrencyCreateDto)
    fun getOne(id: Long): CurrencyResponse
    fun update(id: Long, update: CurrencyUpdateRequest)
    fun delete(id: Long)
}

@Service
class CurrencyServiceImpl(
    private val repository: CurrencyRepository,
) : CurrencyService{
    override fun create(create: CurrencyCreateDto) {
        repository.existsCurrencyByNameAndDeletedFalse(create.name).takeIf { it }?.let {
            throw CurrencyAlreadyExistsException()
        }
        repository.save(Currency(
            create.name,
        ))
    }

    override fun getOne(id: Long): CurrencyResponse {
        repository.findByIdAndDeletedFalse(id)?.let { currency ->
            return CurrencyResponse(
                currency.id!!,
                currency.name,
                currency.createdBy
            )
        }
        throw CurrencyNotFoundException()
    }

    override fun update(id: Long, update: CurrencyUpdateRequest) {
        repository.existsCurrencyByNameAndDeletedFalseAndIdNot(update.name, id).takeIf { it }?.let {
            throw CurrencyAlreadyExistsException()
        }

        repository.findByIdAndDeletedFalse(id)?.let { currency ->
            currency.name = update.name
            repository.save(currency)
        }
        throw CurrencyNotFoundException()
    }

    override fun delete(id: Long) {
        repository.trash(id) ?: throw CurrencyNotFoundException()
    }
}
//Currency