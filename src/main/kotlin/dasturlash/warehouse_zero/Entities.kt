package dasturlash.warehouse_zero

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.util.Date


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP)var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP)var lastModifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
)
//Warehouse
@Entity
@Table(name = "warehouse")
class Warehouse(
    @Column(nullable = false) var name: String,
) : BaseEntity()

//Category
@Entity
@Table(name = "category")
class Category(
    @Column(nullable = false, unique = true) var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category?,
) : BaseEntity()

//Measurement
@Entity
@Table(name = "measurement")
class Measurement(
    @Column(nullable = false) var name: String,
) : BaseEntity()

//Supplier
@Entity
@Table(name = "supplier")
class Supplier(
    @Column(nullable = false) var name: String,
    @Column(nullable = false, unique = true) var phoneNumber: String,
) : BaseEntity()

//Currency
@Entity
@Table(name = "currency")
class Currency(
    @Column(nullable = false) var name: String,
) : BaseEntity()

//Employee
@Entity
@Table(name = "employee")
class Employee(
    @Column(nullable = false) var firstName: String,
    @Column(nullable = false) var lastName: String,
    @Column(nullable = false) var phoneNumber: String,
    @Column(nullable = false) var uniqueNumber: String,
    @Column(nullable = false) var password: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var warehouse: Warehouse,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) var role: Role,
) : BaseEntity()

//Attach
@Entity
@Table(name = "attach")
class Attach(
    @Column(nullable = false, name = "origin_name") var originName: String?,
    @Column(nullable = false) var size: Long,
    @Column(nullable = false) var type: String?,
    @Column(nullable = false) var path: String,
    @Column(nullable = false, unique = true) var hash: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product
) : BaseEntity()

//Product
@Entity
@Table(name = "product")
class Product(
    @Column(nullable = false) var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @Column(nullable = false, unique = true) var productNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_id", nullable = false)
    var measurement: Measurement,

) : BaseEntity()

//Stock in
@Entity
@Table(name = "stock_in")
class StockIn(
    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP) var date: Date,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var warehouse: Warehouse,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    var supplier: Supplier,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    var currency: Currency,

    @Column(nullable = false)var factualNumber: Long,
    @Column(nullable = false, unique = true)var uniqueNumber: Long,
    @Column(nullable = false) var amount: BigDecimal
) : BaseEntity()

//Stock in item
@Entity
@Table(name = "stock_in_items")
class StockInItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_in_id", nullable = false)
    var stockIn: StockIn,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)var measurementCount: Int,
    @Column(nullable = false)var inPrice: BigDecimal,
    @Column(nullable = false)var outPrice: BigDecimal,
    var expireDate: Date? = null,
) : BaseEntity()

//StockOut
@Entity
@Table(name = "stock_out")
class StockOut(
    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP) var date: Date,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var warehouse: Warehouse,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    var currency: Currency,

    @Column(nullable = false)var factualNumber: Long,
    @Column(nullable = false, unique = true)var uniqueNumber: Long,
    @Column(nullable = false)var amount: BigDecimal,
) : BaseEntity()

//Stock out item
@Entity
@Table(name = "stock_out_items")
class StockOutItem(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_out_id", nullable = false)
    var stockOut: StockOut,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)var measurementCount: Int,
    @Column(nullable = false)var factualNumber: Long,
    @Column(nullable = false, unique = true)var uniqueNumber: Long,
    @Column(nullable = false)var amount: BigDecimal,
) : BaseEntity()

//Warehouse product balance
@Entity
@Table(name = "warehouse_products_balance",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["warehouse_id","product_id"])
    ])
class WarehouseProductsBalance(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var warehouse: Warehouse,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)
    var quantity: Int
) : BaseEntity()




