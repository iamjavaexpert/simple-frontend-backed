package com.example.service

import com.example.dao.ProductDao
import com.example.dto.ProductDto
import com.example.dto.VariantDto
import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.sql.Timestamp

/**
 * Service class for handling product-related business logic and external API synchronization.
 *
 * It interacts with the ProductDao for persistence and RestTemplate for external product imports.
 */
@Service
@EnableScheduling
class ProductService(
    private val restTemplate: RestTemplate,
    private val productDAO: ProductDao
) {
    private val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)

    /**
     * Scheduled task that fetches and saves the first 10 products from an external API
     * only if no products exist in the database.
     */
    @Scheduled(initialDelay = 0)
    fun fetchAndSaveProducts() {
        try {
            logger.info("Executing fetchAndSaveProducts()")

            if (productDAO.count() == 0L) {
                logger.info("No products in DB. Fetching from external API...")

                val response = restTemplate.getForObject("https://famme.no/products.json", JsonNode::class.java)
                response?.get("products")?.take(10)?.forEach { productNode ->
                    val now = Timestamp(System.currentTimeMillis())
                    val productId = productNode["id"].asLong()

                    val variants = productNode["variants"].map { variantNode ->
                        VariantDto(
                            id = variantNode["id"].asLong(),
                            productId = productId,
                            title = variantNode["title"].asText(),
                            sku = variantNode["sku"].asText(),
                            price = variantNode["price"].asDouble(),
                            available = variantNode["available"].asBoolean(),
                            option1 = variantNode["option1"].asText(),
                            option2 = variantNode["option2"].asText(),
                            createdAt = now,
                            updatedAt = now
                        )
                    }

                    val productDto = ProductDto(
                        id = productId,
                        title = productNode["title"].asText(),
                        vendor = productNode["vendor"].asText(),
                        type = productNode["product_type"].asText(),
                        variants = variants,
                        createdAt = now,
                        updatedAt = now
                    )

                    productDAO.save(productDto)
                    logger.info("Product saved: ${productDto.title} (ID: $productId)")
                }
            } else {
                logger.info("Products already exist. Skipping fetch.")
            }
        } catch (e: Exception) {
            logger.error("Error during fetchAndSaveProducts(): ${e.message}", e)
        }
    }

    /**
     * Retrieves all products along with their variants from the database.
     */
    fun getAllProducts(): List<ProductDto> {
        logger.info("Fetching all products")
        return productDAO.findAll()
    }

    /**
     * Retrieves all products sorted by a given field and direction.
     *
     * @param sortField The field to sort by (e.g., title, vendor).
     * @param direction The sort direction, either "asc" or "desc".
     */
    fun findAllSortedBy(sortField: String, direction: String): List<ProductDto> {
        logger.info("Fetching products sorted by $sortField $direction")
        return productDAO.findAllSortedBy(sortField, direction)
    }

    /**
     * Saves a new product and its variants to the database.
     *
     * @param productDto The product data to save.
     * @return The generated or provided ID of the saved product.
     */
    fun saveProduct(productDto: ProductDto): Long {
        logger.info("Saving product: ${productDto.title}")
        return productDAO.save(productDto)
    }

    /**
     * Updates an existing product and its variants in the database.
     *
     * @param productDto The updated product data.
     * @param id The ID of the product to update.
     */
    fun updateProduct(productDto: ProductDto, id: Long) {
        logger.info("Updating product ID $id")
        productDAO.update(productDto, id)
    }

    /**
     * Searches for products whose titles contain the given keyword, ignoring case.
     *
     * @param title The title keyword to search for.
     * @return List of matching products.
     */
    fun findByTitleContaining(title: String): List<ProductDto> {
        logger.info("Searching for products with title containing: $title")
        return productDAO.findByTitleContainingIgnoreCase(title)
    }

    /**
     * Retrieves a product and its variants by its ID.
     *
     * @param id The ID of the product.
     * @return The product if found.
     * @throws NoSuchElementException if the product does not exist.
     */
    fun getProductById(id: Long): ProductDto {
        logger.info("Fetching product by ID: $id")
        return productDAO.findById(id)
    }

    /**
     * Deletes a product and its variants from the database by its ID.
     *
     * @param id The ID of the product to delete.
     */
    fun deleteById(id: Long) {
        logger.info("Deleting product ID: $id")
        productDAO.deleteById(id)
    }
}
