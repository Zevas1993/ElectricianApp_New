package com.example.electricianappnew.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Basic data class for a Client
@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val id: String = "client_${System.currentTimeMillis()}",
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "contact_person") val contactPerson: String = "",
    @ColumnInfo(name = "phone") val phone: String = "",
    @ColumnInfo(name = "email") val email: String = "",
    @ColumnInfo(name = "primary_address") val primaryAddress: String = "",
    @ColumnInfo(name = "billing_address") val billingAddress: String = "",
    @ColumnInfo(name = "notes") val notes: String = ""
    // Consider adding fields like: default_labor_rate, tax_exempt
)
