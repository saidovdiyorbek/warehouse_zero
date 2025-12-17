package dasturlash.warehouse_zero.expireNotification

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class ExpireNotificationBot(
    @Value("\${telegram.bot.token}")
    token: String,
    @Value("\${telegram.bot.username}")
    val username: String
) : TelegramLongPollingBot(token) {

    override fun getBotUsername(): String = username

    fun sendMessage(chatId: Long, text: String) {
        val message = SendMessage(chatId.toString(), text)
        execute(message)
        println(chatId)
    }

    override fun onUpdateReceived(p0: Update?) {
        TODO("Not yet implemented")
    }
}
