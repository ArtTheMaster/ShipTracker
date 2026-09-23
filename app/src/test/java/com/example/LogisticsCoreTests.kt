package com.example

import com.example.data.model.CourierType
import com.example.data.model.PlatformType
import com.example.data.model.ShipmentStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.repository.LogisticsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogisticsCoreTests {

    @Test
    fun testPlatformAndCourierPatternClassification() {
        // Shopee SPX
        val shopeeResult = LogisticsRepository.classifyBarcode("SPXPH0492819283")
        assertEquals(PlatformType.SHOPEE, shopeeResult.platform)
        assertEquals(CourierType.SPX, shopeeResult.courier)

        // Lazada LEX
        val lazadaResult = LogisticsRepository.classifyBarcode("LX092817291PH")
        assertEquals(PlatformType.LAZADA, lazadaResult.platform)
        assertEquals(CourierType.LAZADA_LEX, lazadaResult.courier)

        // TikTok Shop
        val tikTokResult = LogisticsRepository.classifyBarcode("TT982716253PH")
        assertEquals(PlatformType.TIKTOK_SHOP, tikTokResult.platform)
        assertEquals(CourierType.J_AND_T, tikTokResult.courier)

        // J&T Express
        val jtResult = LogisticsRepository.classifyBarcode("JT8291029381PH")
        assertEquals(CourierType.J_AND_T, jtResult.courier)

        // Flash Express
        val flashResult = LogisticsRepository.classifyBarcode("FL992817265PH")
        assertEquals(CourierType.FLASH_EXPRESS, flashResult.courier)

        // Ninja Van
        val ninjaResult = LogisticsRepository.classifyBarcode("NV881920192PH")
        assertEquals(CourierType.NINJA_VAN, ninjaResult.courier)
    }

    @Test
    fun testStrictStateMachineValidTransitions() {
        // SCANNED -> PREPARED: OK
        assertTrue(LogisticsRepository.isValidTransition(ShipmentStatus.SCANNED, ShipmentStatus.PREPARED))

        // PREPARED -> DISPATCHED: OK
        assertTrue(LogisticsRepository.isValidTransition(ShipmentStatus.PREPARED, ShipmentStatus.DISPATCHED))

        // DISPATCHED -> IN_TRANSIT: OK
        assertTrue(LogisticsRepository.isValidTransition(ShipmentStatus.DISPATCHED, ShipmentStatus.IN_TRANSIT))

        // IN_TRANSIT -> DELIVERED: OK
        assertTrue(LogisticsRepository.isValidTransition(ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED))

        // IN_TRANSIT -> RETURNED: OK
        assertTrue(LogisticsRepository.isValidTransition(ShipmentStatus.IN_TRANSIT, ShipmentStatus.RETURNED))
    }

    @Test
    fun testStrictStateMachineInvalidTransitionsViolations() {
        // Direct jump SCANNED -> DELIVERED (SOP violation: skipped packing & dispatch handover)
        assertFalse(LogisticsRepository.isValidTransition(ShipmentStatus.SCANNED, ShipmentStatus.DELIVERED))

        // Direct jump SCANNED -> DISPATCHED (SOP violation: skipped packing inspection)
        assertFalse(LogisticsRepository.isValidTransition(ShipmentStatus.SCANNED, ShipmentStatus.DISPATCHED))

        // Terminal state modification: DELIVERED -> SCANNED (Forbidden)
        assertFalse(LogisticsRepository.isValidTransition(ShipmentStatus.DELIVERED, ShipmentStatus.SCANNED))

        // Terminal state modification: RETURNED -> PREPARED (Forbidden)
        assertFalse(LogisticsRepository.isValidTransition(ShipmentStatus.RETURNED, ShipmentStatus.PREPARED))
    }

    @Test
    fun testRoleBasedAccessControlRules() {
        val admin = UserAccount.DEFAULT_ADMIN
        val staff = UserAccount.STAFF_GJ

        assertEquals(UserRole.OWNER_ADMIN, admin.role)
        assertEquals(UserRole.STAFF_PACKER, staff.role)

        assertTrue(admin.role.canDeleteShipments)
        assertFalse(staff.role.canDeleteShipments)

        assertTrue(admin.role.canViewFinancialMetrics)
        assertFalse(staff.role.canViewFinancialMetrics)

        assertTrue(admin.role.canScanParcels)
        assertTrue(staff.role.canScanParcels)

        assertTrue(admin.role.canHandoverBatches)
        assertTrue(staff.role.canHandoverBatches)
    }
}
