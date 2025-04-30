package com.example.electricianappnew.data.model

import java.util.UUID // Add UUID import

// Define data classes used across multiple calculators here

data class WireEntry(
    val id: String = UUID.randomUUID().toString(), // Use UUID for stable ID
    var type: String = "", // e.g., "THHN", "XHHW"
    var size: String = "", // e.g., "12 AWG", "250 kcmil"
    var quantity: Int = 1
    // Removed availableSizes property
)

// Add other common calculator data models here if needed in the future
// e.g., ResistanceEntry if used by more than one screen
