package com.example.dao

import com.example.constants.QueryConstants
import com.example.dto.ProductDto
import com.example.dto.VariantDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

/**
 * Repository class responsible for handling database operations related to products and their variants.
 * This class uses JdbcClient for executing SQL queries and mapping results to domain objects.
 *
 * @param jdbcClient The JdbcClient used to interact with the database
 */
@Repository
@Transactional(readOnly = true)
class ProductDao(private val jdbcClient: JdbcClient) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ProductDao::class.java)
        private val VALID_SORT_FIELDS = setOf("title", "vendor", "type", "updated_at")
        private const val DEFAULT_SORT_FIELD = "updated_at"


    }

    /**
     * Retrieves all products from the database along with their variants.
     *
     * @return List of all products with their associated variants
     */
    fun findAll(): List<ProductDto> {
        logger.debug("Fetching all products")

        val products = executeProductQuery(QueryConstants.GET_ALL_PRODUCTS)
        return enrichProductsWithVariants(products)
    }

    /**
     * Retrieves all products sorted by the specified field and direction.
     *
     * @param sortField The field to sort by (must be in VALID_SORT_FIELDS)
     * @param direction The sort direction ("asc" or "desc")
     * @return List of sorted products with their variants
     */
    fun findAllSortedBy(sortField: String, direction: String): List<ProductDto> {
        logger.debug("Fetching all products sorted by {} {}", sortField, direction)

        val validatedSortField = if (sortField in VALID_SORT_FIELDS) sortField else DEFAULT_SORT_FIELD
        val sortDirection = if (direction.equals("asc", ignoreCase = true)) "ASC" else "DESC"

        val sql = "SELECT * FROM products ORDER BY $validatedSortField $sortDirection"
        val products = executeProductQuery(sql)

        return enrichProductsWithVariants(products)
    }

    /**
     * Saves a product and its associated variants to the database.
     *
     * @param productDto The product data to save
     * @return The ID of the saved product
     */
    @Transactional
    fun save(productDto: ProductDto): Long {
        logger.debug("Saving product: {}", productDto.title)

        val now = LocalDateTime.now()
        val productId = generateIdIfNeeded(productDto.id)

        saveProductEntity(productDto, productId, now)
        saveVariants(productDto.variants, productId, now)

        logger.debug("Successfully saved product with ID: {}", productId)
        return productId
    }

    /**
     * Updates an existing product and its variants.
     *
     * @param productDto The updated product data
     * @param id The ID of the product to update
     */
    @Transactional
    fun update(productDto: ProductDto, id: Long) {
        logger.debug("Updating product with ID: {}", id)

        val now = LocalDateTime.now()

        updateProductEntity(productDto, id, now)
        updateVariants(productDto.variants, id, now)

        logger.debug("Successfully updated product with ID: {}", id)
    }

    /**
     * Finds a product by its ID.
     *
     * @param id The product ID to search for
     * @return The product with its variants
     * @throws NoSuchElementException if the product is not found
     */
    fun findById(id: Long): ProductDto {
        logger.debug("Finding product by ID: {}", id)

        val params = MapSqlParameterSource("id", id)
        val products = jdbcClient.sql(QueryConstants.FIND_PRODUCT_BY_ID)
            .paramSource(params)
            .query(::mapProductRow)
            .list()

        val product = products.firstOrNull()
            ?: throw NoSuchElementException("Product with ID $id not found")

        val variants = findVariantsByProductId(id)
        return product.copy(variants = variants)
    }

    /**
     * Searches for products by title using case-insensitive partial matching.
     *
     * @param title The title substring to search for
     * @return List of matching products with their variants
     */
    fun findByTitleContainingIgnoreCase(title: String): List<ProductDto> {
        logger.debug("Searching products by title: {}", title)

        val likeTitle = "%$title%"
        val params = MapSqlParameterSource("title", likeTitle)

        val products = jdbcClient.sql(QueryConstants.FIND_PRODUCTS_BY_TITLE_LIKE)
            .paramSource(params)
            .query(::mapProductRow)
            .list()

        return enrichProductsWithVariants(products)
    }

    /**
     * Deletes a product and all its variants by ID.
     *
     * @param id The ID of the product to delete
     */
    @Transactional
    fun deleteById(id: Long) {
        logger.debug("Deleting product with ID: {}", id)

        val params = MapSqlParameterSource("id", id)

        // Delete variants first due to foreign key constraints
        jdbcClient.sql(QueryConstants.DELETE_VARIANTS_BY_PRODUCT_ID)
            .paramSource(params)
            .update()

        jdbcClient.sql(QueryConstants.DELETE_PRODUCT_BY_ID)
            .paramSource(params)
            .update()

        logger.debug("Successfully deleted product with ID: {}", id)
    }

    /**
     * Returns the total count of products in the database.
     *
     * @return The number of products
     */
    fun count(): Long {
        logger.debug("Counting total products")

        return jdbcClient.sql(QueryConstants.PRODUCT_COUNT)
            .query(Long::class.java)
            .single()
    }

    /**
     * Checks if a product exists by ID.
     *
     * @param id The product ID to check
     * @return true if the product exists, false otherwise
     */
    fun existsById(id: Long): Boolean {
        return try {
            jdbcClient.sql(QueryConstants.EXISTS_PRODUCT_BY_ID)
                .param("id", id)
                .query(Int::class.java)
                .single()
            true
        } catch (e: EmptyResultDataAccessException) {
            false
        }
    }

    // Private helper methods

    private fun executeProductQuery(sql: String, params: MapSqlParameterSource? = null): List<ProductDto> {
        val query = jdbcClient.sql(sql)
        return if (params != null) {
            query.paramSource(params)
        } else {
            query
        }.query(::mapProductRow).list()
    }

    private fun enrichProductsWithVariants(products: List<ProductDto>): List<ProductDto> {
        if (products.isEmpty()) return emptyList()

        return products.map { product ->
            val variants = findVariantsByProductId(product.id)
            product.copy(variants = variants)
        }
    }

    private fun findVariantsByProductId(productId: Long): List<VariantDto> {
        val params = MapSqlParameterSource("id", productId)
        return jdbcClient.sql(QueryConstants.GET_ALL_VARIANTS_OF_A_PRODUCT)
            .paramSource(params)
            .query(::mapVariantRow)
            .list()
    }

    private fun mapProductRow(rs: ResultSet, rowNum: Int): ProductDto {
        return ProductDto(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            vendor = rs.getString("vendor"),
            type = rs.getString("type"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at")
        )
    }

    private fun mapVariantRow(rs: ResultSet, rowNum: Int): VariantDto {
        return VariantDto(
            id = rs.getLong("id"),
            productId = rs.getLong("product_id"),
            title = rs.getString("title"),
            sku = rs.getString("sku"),
            price = rs.getDouble("price"),
            available = rs.getBoolean("available"),
            option1 = rs.getString("option1"),
            option2 = rs.getString("option2"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at")
        )
    }

    private fun generateIdIfNeeded(existingId: Long?): Long {
        return if (existingId == null || existingId == 0L) {
            System.currentTimeMillis() + Random().nextInt(1000)
        } else {
            existingId
        }
    }

    private fun saveProductEntity(productDto: ProductDto, productId: Long, timestamp: LocalDateTime) {
        val params = MapSqlParameterSource()
            .addValue("id", productId)
            .addValue("title", productDto.title)
            .addValue("vendor", productDto.vendor)
            .addValue("type", productDto.type)
            .addValue("created_at", timestamp)
            .addValue("updated_at", timestamp)

        jdbcClient.sql(QueryConstants.SAVE_PRODUCT)
            .paramSource(params)
            .update()
    }

    private fun saveVariants(variants: List<VariantDto>, productId: Long, timestamp: LocalDateTime) {
        variants.forEach { variant ->
            val variantId = generateIdIfNeeded(variant.id)
            val params = createVariantParams(variant, variantId, productId, timestamp, timestamp)

            jdbcClient.sql(QueryConstants.SAVE_VARIANT)
                .paramSource(params)
                .update()
        }
    }

    private fun updateProductEntity(productDto: ProductDto, id: Long, timestamp: LocalDateTime) {
        jdbcClient.sql(QueryConstants.UPDATE_PRODUCT)
            .param("id", id)
            .param("title", productDto.title)
            .param("vendor", productDto.vendor)
            .param("type", productDto.type)
            .param("updated_at", timestamp)
            .update()
    }

    private fun updateVariants(variants: List<VariantDto>, productId: Long, timestamp: LocalDateTime) {
        val (existingVariants, newVariants) = variants.partition { it.id > 0 }

        updateExistingVariants(existingVariants, productId, timestamp)
        deleteOrphanedVariants(existingVariants.map { it.id }, productId)
        insertNewVariants(newVariants, productId, timestamp)
    }

    private fun updateExistingVariants(variants: List<VariantDto>, productId: Long, timestamp: LocalDateTime) {
        variants.forEach { variant ->
            jdbcClient.sql(QueryConstants.UPDATE_VARIANT)
                .param("id", variant.id)
                .param("product_id", productId)
                .param("title", variant.title)
                .param("sku", variant.sku)
                .param("price", variant.price)
                .param("option1", variant.option1)
                .param("option2", variant.option2)
                .param("available", variant.available)
                .param("updated_at", timestamp)
                .update()
        }
    }

    private fun insertNewVariants(variants: List<VariantDto>, productId: Long, timestamp: LocalDateTime) {
        variants.forEach { variant ->
            val generatedId = generateIdIfNeeded(null)
            val params = createVariantParams(variant, generatedId, productId, timestamp, timestamp)

            jdbcClient.sql(QueryConstants.SAVE_VARIANT)
                .paramSource(params)
                .update()
        }
    }

    private fun deleteOrphanedVariants(incomingVariantIds: List<Long>, productId: Long) {
        val dbVariantIds = jdbcClient.sql(QueryConstants.GET_VARIANT_IDS_BY_PRODUCT_ID)
            .param("product_id", productId)
            .query(Long::class.java)
            .list()

        val idsToDelete = dbVariantIds.filterNot { it in incomingVariantIds }

        if (idsToDelete.isNotEmpty()) {
            jdbcClient.sql(QueryConstants.DELETE_VARIANTS_BY_IDS)
                .param("ids", idsToDelete)
                .update()
        }
    }

    private fun createVariantParams(
        variant: VariantDto,
        variantId: Long,
        productId: Long,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime
    ): MapSqlParameterSource {
        return MapSqlParameterSource()
            .addValue("id", variantId)
            .addValue("product_id", productId)
            .addValue("title", variant.title)
            .addValue("sku", variant.sku)
            .addValue("price", variant.price)
            .addValue("option1", variant.option1)
            .addValue("option2", variant.option2)
            .addValue("available", variant.available)
            .addValue("created_at", createdAt)
            .addValue("updated_at", updatedAt)
    }
}