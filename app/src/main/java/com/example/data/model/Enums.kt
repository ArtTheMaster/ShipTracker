package com.example.data.model

enum class UserRole(val displayName: String, val description: String) {
    OWNER_ADMIN(
        displayName = "Owner / Admin",
        description = "Full control: analytics, reports, courier configs, staff management, returns"
    ),
    STAFF_PACKER(
        displayName = "Staff / Packer-Scanner",
        description = "Operations: scanning, packing, dispatch batches, shipment status, return logging"
    );

    val canDeleteShipments: Boolean
        get() = this == OWNER_ADMIN

    val canViewFinancialMetrics: Boolean
        get() = this == OWNER_ADMIN

    val canScanParcels: Boolean
        get() = true

    val canHandoverBatches: Boolean
        get() = true
}

enum class ShipmentStatus(val displayName: String, val stepIndex: Int) {
    SCANNED("Scanned", 0),
    PREPARED("Prepared / Packed", 1),
    DISPATCHED("Dispatched", 2),
    IN_TRANSIT("In-Transit", 3),
    DELIVERED("Delivered", 4),
    RETURNED("Returned (RTS)", 5),
    CANCELLED("Cancelled", 6);

    val isTerminal: Boolean
        get() = this == DELIVERED || this == RETURNED || this == CANCELLED
}

enum class PlatformType(val displayName: String, val badgeColorHex: Long) {
    SHOPEE("Shopee", 0xFFEE4D2D),
    LAZADA("Lazada", 0xFF0F146D),
    TIKTOK_SHOP("TikTok Shop", 0xFF111111),
    OTHER("Direct / Other", 0xFF475569)
}

enum class CourierType(val displayName: String, val brandColorHex: Long) {
    SPX("Shopee Xpress (SPX)", 0xFFEE4D2D),
    J_AND_T("J&T Express", 0xFFE30613),
    FLASH_EXPRESS("Flash Express", 0xFFEAB308),
    NINJA_VAN("Ninja Van", 0xFFC2002F),
    LAZADA_LEX("Lazada (LEX)", 0xFF0284C7),
    OTHER("Other Courier", 0xFF64748B)
}

enum class ReturnReason(val label: String) {
    CUSTOMER_REJECTED_COD("Customer Rejected / COD Refusal"),
    DAMAGED_IN_TRANSIT("Damaged in Transit"),
    WRONG_ITEM_SENT("Wrong Item / Wrong Variation"),
    INCOMPLETE_ADDRESS("Address Incomplete / Unreachable"),
    FRAUDULENT_ORDER("Suspected Fraud / Fake Order"),
    BUYER_CANCELLED("Buyer Cancelled Late"),
    OTHER("Other Logistics Issue")
}

enum class ItemCondition(val label: String) {
    PRISTINE_RESELLABLE("Pristine / Resellable (Grade A)"),
    DAMAGED_PACKAGING("Packaging Damaged / Item OK (Grade B)"),
    SEVERELY_DAMAGED("Severely Damaged / Total Loss (Grade C)")
}

enum class RefundStatus(val label: String) {
    PENDING_VERIFICATION("Pending Inspection"),
    REFUND_APPROVED("Refund Approved"),
    DISPUTE_RAISED("Dispute Raised with Platform"),
    CLAIM_FILED_COURIER("Courier Claim Filed"),
    REJECTED("Refund Rejected")
}

enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
    CONFLICT
}
