package com.example.electricianappnew.data.model

// Define data classes used across multiple calculators here

data class WireEntry(
    val id: Int = System.identityHashCode(Any()),
    var type: String = "", // Initialize empty, set defaults in ViewModel init
    var size: String = "", // Initialize empty, set defaults in ViewModel init
    var quantity: Int = 1
)

// Add other common calculator data models here if needed in the future
// e.g., ResistanceEntry if used by more than one screen
