package dasturlash.warehouse_zero

enum class Status {
    ACTIVE, INACTIVE
}

enum class Role {
    EMPLOYEE, ADMIN
}

enum class ErrorCode(val code: Int, val message: String) {
    WAREHOUSE_ALREADY_EXISTS(100, "WAREHOUSE_ALREADY_EXISTS"),
    WAREHOUSE_NOT_FOUND(101, "WAREHOUSE_NOT_FOUND"),
    EMPLOYEE_NOT_FOUND(200, "EMPLOYEE_NOT_FOUND"),
}