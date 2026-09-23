package com.example.data.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromShipmentStatus(value: ShipmentStatus?): String? = value?.name

    @TypeConverter
    fun toShipmentStatus(value: String?): ShipmentStatus? =
        value?.let { runCatching { ShipmentStatus.valueOf(it) }.getOrDefault(ShipmentStatus.SCANNED) }

    @TypeConverter
    fun fromPlatformType(value: PlatformType?): String? = value?.name

    @TypeConverter
    fun toPlatformType(value: String?): PlatformType? =
        value?.let { runCatching { PlatformType.valueOf(it) }.getOrDefault(PlatformType.OTHER) }

    @TypeConverter
    fun fromCourierType(value: CourierType?): String? = value?.name

    @TypeConverter
    fun toCourierType(value: String?): CourierType? =
        value?.let { runCatching { CourierType.valueOf(it) }.getOrDefault(CourierType.OTHER) }

    @TypeConverter
    fun fromReturnReason(value: ReturnReason?): String? = value?.name

    @TypeConverter
    fun toReturnReason(value: String?): ReturnReason? =
        value?.let { runCatching { ReturnReason.valueOf(it) }.getOrDefault(ReturnReason.OTHER) }

    @TypeConverter
    fun fromItemCondition(value: ItemCondition?): String? = value?.name

    @TypeConverter
    fun toItemCondition(value: String?): ItemCondition? =
        value?.let { runCatching { ItemCondition.valueOf(it) }.getOrDefault(ItemCondition.PRISTINE_RESELLABLE) }

    @TypeConverter
    fun fromRefundStatus(value: RefundStatus?): String? = value?.name

    @TypeConverter
    fun toRefundStatus(value: String?): RefundStatus? =
        value?.let { runCatching { RefundStatus.valueOf(it) }.getOrDefault(RefundStatus.PENDING_VERIFICATION) }

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus?): String? = value?.name

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? =
        value?.let { runCatching { SyncStatus.valueOf(it) }.getOrDefault(SyncStatus.SYNCED) }
}
