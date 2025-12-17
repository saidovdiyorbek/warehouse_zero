package dasturlash.warehouse_zero.security

import dasturlash.warehouse_zero.EmployeeNotAuthenticatedException
import dasturlash.warehouse_zero.Role
import dasturlash.warehouse_zero.Status
import dasturlash.warehouse_zero.UserDetailsResponse
import jakarta.persistence.criteria.Root
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityUtils {

    fun getCurrentUser(): UserDetailsResponse? {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.principal as UserDetailsResponse?
    }

    fun getCurrentUserId(): Long{
        return getCurrentUser()?.id
            ?: throw EmployeeNotAuthenticatedException()
    }

    fun getCurrentUserName(): String {
        return getCurrentUser()?.username
            ?: throw EmployeeNotAuthenticatedException()
    }

    fun getCurrentUserRole(): Role {
        return getCurrentUser()?.role
            ?: throw RuntimeException("User not authenticated")
    }

    fun isAdmin(): Boolean {
        return getCurrentUser()?.role == Role.ROLE_ADMIN
    }

    /*fun isUserActive(): Boolean {
        return getCurrentUser()?. == Status.ACTIVE
    }*/
}