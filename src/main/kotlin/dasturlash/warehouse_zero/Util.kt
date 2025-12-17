package dasturlash.warehouse_zero

import org.springframework.stereotype.Component

@Component
class GenerateHash(){

    fun generateHash(): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString =  (1..10)
            .map {i -> kotlin.random.Random.nextInt(0, charPool.size).let { charPool[it] }}
            .joinToString("")
        return randomString
    }
}