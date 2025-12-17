package dasturlash.warehouse_zero.expireNotification

import dasturlash.warehouse_zero.ProductRepository
import dasturlash.warehouse_zero.StockInItemRepository
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Component
@EnableScheduling
class ExpireCheckJob(
    private val stockInItem: StockInItemRepository,
    private val productRepository: ProductRepository,
    private val bot: ExpireNotificationBot
) {

    private val chatId = 1708471433L // group yoki admin chatId

    @Scheduled(cron = "0 59 14 * * *") // har kuni 09:00
    fun checkExpireDates() {
        val today = LocalDate.now()

        val products = stockInItem.findAll()

        products.forEach { stockInItem ->

            val product = productRepository.findByIdAndDeletedFalse(stockInItem.product.id!!)
            val daysLeft = ChronoUnit.DAYS.between(today, stockInItem.expireDate)
            println(daysLeft)
            if (daysLeft == stockInItem.notifyBeforeDay.toLong()) {
                bot.sendMessage(
                    chatId,
                    """
                     *Product expire warning*
                    
                     Product: ${product?.name}
                     Expire date: ${stockInItem.expireDate}
                     Qolgan kun: $daysLeft
                    """.trimIndent()
                )
            }
        }
    }
}
