package com.example.dto

import java.sql.Timestamp

/**
 * Data Transfer Object (DTO) for product details.
 *
 * This class is used to encapsulate the information related to a product,
 * such as the product's title, vendor, type, and associated variants.
 * It is typically used to transfer product data between the client and server.
 *
 * @property title The title or name of the product.
 * @property vendor The vendor or manufacturer of the product.
 * @property type The type/category of the product.
 * @property variants A list of variant details associated with the product.
 */
data class ProductDto(
    val id: Long = 0,
    val title: String = "",
    val vendor: String = "",
    val type: String = "",
    val variants: List<VariantDto> = listOf(VariantDto()),
    val createdAt: Timestamp = Timestamp(System.currentTimeMillis()),
    val updatedAt: Timestamp = Timestamp(System.currentTimeMillis())
)