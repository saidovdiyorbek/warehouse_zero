package dasturlash.warehouse_zero

import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.Locale
import org.springframework.validation.FieldError

@RestControllerAdvice
class ExceptionHandler(
    private val messageSource: MessageSource
){
    @ExceptionHandler(WarehouseAppException::class)
    fun handleWarehouseAppException(ex: WarehouseAppException): ResponseEntity<BaseMessage> {
        val local = LocaleContextHolder.getLocale()
        val message = try {
            messageSource.getMessage(ex.errorType().toString(), null, local)
        }catch (e: NoSuchMessageException){
            ex.errorType().toString().replace("_", " ").lowercase()
        }

        return ResponseEntity
            .badRequest()
            .body(BaseMessage(ex.errorType().code, message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<BaseMessage> {
        val filedError: FieldError = ex.bindingResult.allErrors.first() as FieldError

        val local = LocaleContextHolder.getLocale()
        val errorMessage = filedError.defaultMessage ?: "Validation error"

        val message = try {
            messageSource.getMessage(errorMessage, null, local)
        }catch (e: NoSuchMessageException) {
            errorMessage.replace("_", " ").lowercase()
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseMessage(
                code = 400,
                message = "${filedError.field}: $message"
            ))
    }
}


sealed class WarehouseAppException(message: String? = null) : RuntimeException() {
    abstract fun errorType(): ErrorCode
    protected open fun getErrorMessageArguments(): Array<Any?>? = null
    fun gerErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        return BaseMessage(
            code = errorType().code,
            message = errorMessageSource.getMessage(
                errorType().toString(),
                getErrorMessageArguments() as Array<out Any>?,
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}

class WarehouseAlreadyExistsException() : WarehouseAppException(){
    override fun errorType() = ErrorCode.WAREHOUSE_ALREADY_EXISTS
}

class WarehouseNotFoundException() : WarehouseAppException(){
    override fun errorType() = ErrorCode.WAREHOUSE_NOT_FOUND
}

class EmployeeNotFoundException() : WarehouseAppException() {
    override fun errorType() = ErrorCode.EMPLOYEE_NOT_FOUND

}

class InvalidPasswordException() : WarehouseAppException(){
    override fun errorType() = ErrorCode.INVALID_PASSWORD
}
