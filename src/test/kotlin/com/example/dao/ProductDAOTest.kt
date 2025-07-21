package com.example.dao

import com.example.dto.ProductDto
import com.example.dto.VariantDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.sql.Timestamp
import java.time.LocalDateTime

@JdbcTest
@Import(ProductDao::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = ["spring.flyway.enabled=false"])
class ProductDaoTest {

    @Autowired
    private lateinit var jdbcClient: JdbcClient

    @Autowired
    private lateinit var productDao: ProductDao

    private fun insertProduct(productId: Long = 1L): Long {
        val now = Timestamp.valueOf(LocalDateTime.now())

        jdbcClient.sql(
            """
            INSERT INTO products (id, title, vendor, type, created_at, updated_at)
            VALUES (:id, :title, :vendor, :type, :createdAt, :updatedAt)
        """.trimIndent()
        )
            .param("id", productId)
            .param("title", "Initial Product")
            .param("vendor", "Test Vendor")
            .param("type", "Shoes")
            .param("createdAt", now)
            .param("updatedAt", now)
            .update()

        jdbcClient.sql(
            """
            INSERT INTO variants (id, product_id, title, sku, price, available, option1, option2, created_at, updated_at)
            VALUES (1001, :productId, 'Red / 42', 'SKU123', 199.0, true, 'Red', '42', :now, :now)
        """.trimIndent()
        )
            .param("productId", productId)
            .param("now", now)
            .update()

        return productId
    }

    @Test
    fun `update should update product and handle variants correctly`() {
        val productId = insertProduct()

        val updatedProduct = ProductDto(
            id = productId,
            title = "Updated Product",
            vendor = "Updated Vendor",
            type = "Sneakers",
            variants = listOf(
                VariantDto(
                    id = 1001,
                    productId = productId,
                    title = "Red / 42 Updated",
                    sku = "SKU123-NEW",
                    price = 249.0,
                    available = false,
                    option1 = "Red",
                    option2 = "42"
                ),
                VariantDto(
                    title = "Blue / 43",
                    sku = "SKU999",
                    price = 299.0,
                    available = true,
                    option1 = "Blue",
                    option2 = "43"
                )
            )
        )

        productDao.update(updatedProduct, productId)

        val updatedTitle = jdbcClient.sql("SELECT title FROM products WHERE id = :id")
            .param("id", productId)
            .query(String::class.java)
            .single()

        assertEquals("Updated Product", updatedTitle)

        val variantCount = jdbcClient.sql("SELECT COUNT(*) FROM variants WHERE product_id = :id")
            .param("id", productId)
            .query(Int::class.java)
            .single()

        assertEquals(2, variantCount)

        val updatedSku = jdbcClient.sql("SELECT sku FROM variants WHERE id = 1001")
            .query(String::class.java)
            .single()

        assertEquals("SKU123-NEW", updatedSku)
    }

    @Test
    fun `save should persist product and variants`() {
        val now = LocalDateTime.now()
        val product = ProductDto(
            title = "New Product",
            vendor = "New Vendor",
            type = "Apparel",
            variants = listOf(
                VariantDto(
                    title = "Black / M",
                    sku = "SKU-BLACK-M",
                    price = 149.99,
                    available = true,
                    option1 = "Black",
                    option2 = "M"
                )
            )
        )

        val productId = productDao.save(product)
        assertNotNull(productId)

        val persisted = productDao.findById(productId)
        assertEquals("New Product", persisted.title)
        assertEquals(1, persisted.variants.size)
        assertEquals("SKU-BLACK-M", persisted.variants[0].sku)
    }

    @Test
    fun `findById should throw when product does not exist`() {
        assertThrows(NoSuchElementException::class.java) {
            productDao.findById(9999L)
        }
    }

    @Test
    fun `deleteById should remove product and its variants`() {
        val id = insertProduct()
        productDao.deleteById(id)

        val count = jdbcClient.sql("SELECT COUNT(*) FROM products WHERE id = :id")
            .param("id", id)
            .query(Int::class.java)
            .single()
        assertEquals(0, count)
    }

    @Test
    fun `findByTitleContainingIgnoreCase should find matching titles`() {
        insertProduct()

        val results = productDao.findByTitleContainingIgnoreCase("initial")
        assertTrue(results.any { it.title.contains("Initial", ignoreCase = true) })
    }
}
