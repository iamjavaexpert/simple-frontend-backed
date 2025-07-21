package com.example.dto

import java.sql.Timestamp

/**
 * Data Transfer Object (DTO) for variant details of a product.
 *
 * This class represents the variant information associated with a product,
 * including details such as the variant title, SKU (Stock Keeping Unit), price,
 * availability status, and any additional options for the variant.
 * It is used to transfer variant-related data between the client and server.
 *
 * @property title The title or name of the product variant.
 * @property sku The Stock Keeping Unit (SKU) identifier for the variant.
 * @property price The price of the variant.
 * @property available The availability status of the variant (true if available, false if not).
 * @property option1 The first option associated with the variant (e.g., size, color).
 * @property option2 The second option associated with the variant (e.g., material, style).
 */
data class VariantDto(
    val id: Long = 0,
    val productId: Long = 0,
    val title: String = "",
    val sku: String = "",
    val price: Double = 0.0,
    val available: Boolean = false,
    val option1: String = "",
    val option2: String = "",
    val createdAt: Timestamp = Timestamp(System.currentTimeMillis()),
    val updatedAt: Timestamp = Timestamp(System.currentTimeMillis())
)