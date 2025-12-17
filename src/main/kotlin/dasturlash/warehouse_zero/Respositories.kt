package dasturlash.warehouse_zero

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Date

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
    fun findByIdAndDeletedFalseAndStatusActive(id: Long): T?
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    entityManager: EntityManager
): SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {
    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), true) }
    override fun findByIdAndDeletedFalse(id: Long): T? = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)

    override fun findAllNotDeleted(pageable: Pageable): Page<T> = findAll(isNotDeletedSpecification, pageable)


    override fun findByIdAndDeletedFalseAndStatusActive(id: Long): T? = findByIdOrNull(id)?.run {
        if (deleted && status == Status.ACTIVE) null else this
    }

}

@Repository
interface WarehouseRepository : BaseRepository<Warehouse>{
    @Query("""
        select w from Warehouse w 
        where w.name = ?1 and w.deleted = false 
    """)
    fun existsWarehouseByName(name: String): Warehouse?

    @Query("""
        select w from Warehouse w
        where w.id = ?1 and w.deleted = false and w.status = 'ACTIVE'
    """)
    fun findWarehouseByIdAndActive(id: Long): Warehouse?
}

interface EmployeeRepository : BaseRepository<Employee>{
    fun findByPhoneNumberAndDeletedFalse(phoneNumber: String): Employee?
    fun findByPhoneNumber(phoneNumber: String): Employee?

    @Query("""
        select e 
        from Employee e
        where e.uniqueNumber = ?1
    """)
    fun findByEmployeeUniqueNumber(employeeUnique: String): Employee?
}

interface CategoryRepository : BaseRepository<Category>{

    fun existsCategoryByNameAndDeletedFalse(name: String): Boolean?
}

interface AttachRepository : BaseRepository<Attach>{
    fun existsByHashAndDeletedFalse(hash: String): Boolean
    fun findAttachByHashAndDeletedFalse(hash: String): Attach?
    fun findAllByProductIdAndDeletedFalse(productId: Long): List<Attach>
}
@Repository
interface ProductRepository : BaseRepository<Product>{
    fun existsProductByNameAndDeletedFalse(name: String): Boolean
    /*@Query("""
        select p from Product p
        where p.name = ?1 and p.id != ?2 and p.deleted = false
    """)*/
    fun existsProductByNameAndDeletedFalseAndIdNot(name: String, id: Long): Boolean
    fun existsProductByProductNumber(productNumber: Int): Boolean

}

interface MeasurementRepository : BaseRepository<Measurement>{
    fun existsMeasurementByNameAndDeletedFalse(measurementName: String): Boolean
}

interface StockInItemRepository : BaseRepository<StockInItem>{
    @Query("""
        select st from StockInItem st
        where st.product.id = ?1
        order by st.createdDate desc
        limit 1
    """)
    fun findStockInItemByProductId(productId: Long): StockInItem?

    @Query("""
        select count(p.name)
        from StockInItem sii 
        join Product p on sii.product.id = p.id
        where sii.expireDate = ?1
    """)
    fun findAllExpiredProducts(date: Date): Int
}

interface StockInRepository : BaseRepository<StockIn>{
    @Query("""select 
        p.name, sii.outPrice productName,
        (sii.outPrice * sii.measurementCount) as sum,
        sii.measurementCount measureCount
        from StockIn si
        join StockInItem sii on si.id = sii.stockIn.id
        join Product p on sii.product.id = p.id
        where cast(si.date as date) = ?1
        group by sii.id, p.name, sii.measurementCount""")
    fun findDailyInProductsSum(date: LocalDate): List<DailyInProductProjection>?
}

interface SupplierRepository : BaseRepository<Supplier>{
    fun existsSupplierByPhoneNumber(phoneNumber: String): Boolean
}

interface CurrencyRepository : BaseRepository<Currency>{
    fun existsCurrencyByNameAndDeletedFalse(name: String): Boolean
    fun existsCurrencyByNameAndDeletedFalseAndIdNot(name: String, id: Long): Boolean
}

interface StockOutRepository : BaseRepository<StockOut>{}

interface StockOutItemRepository : BaseRepository<StockOutItem>{}

@Repository
interface WarehouseProductBalanceRepository : BaseRepository<WarehouseProductsBalance>{

    fun findByWarehouseIdAndProductId(warehouseId: Long, productId: Long): WarehouseProductsBalance?
}