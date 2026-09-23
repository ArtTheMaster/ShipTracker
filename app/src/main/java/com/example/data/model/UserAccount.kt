package com.example.data.model

data class UserAccount(
    val id: String,
    val fullName: String,
    val role: UserRole,
    val email: String,
    val staffCode: String,
    val avatarColorHex: Long
) {
    companion object {
        val DEFAULT_ADMIN = UserAccount(
            id = "USR-001",
            fullName = "Nolan (Owner)",
            role = UserRole.OWNER_ADMIN,
            email = "nolan.admin@gjandashershiptracker.com",
            staffCode = "ADM-01",
            avatarColorHex = 0xFF1E3A8A
        )

        val DEFAULT_STAFF = UserAccount(
            id = "USR-002",
            fullName = "G.J. (Packer-Scanner)",
            role = UserRole.STAFF_PACKER,
            email = "gj.scanner@gjandashershiptracker.com",
            staffCode = "OPR-04",
            avatarColorHex = 0xFFF97316
        )

        val STAFF_GJ = DEFAULT_STAFF

        val ALL_ACCOUNTS = listOf(
            DEFAULT_ADMIN,
            DEFAULT_STAFF,
            UserAccount(
                id = "USR-003",
                fullName = "Maria Santos (Fulfillment Staff)",
                role = UserRole.STAFF_PACKER,
                email = "maria.s@gjandashershiptracker.com",
                staffCode = "OPR-07",
                avatarColorHex = 0xFF0D9488
            )
        )
    }
}
