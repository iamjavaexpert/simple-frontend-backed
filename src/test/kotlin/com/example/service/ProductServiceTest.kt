package com.example.service

import com.example.dao.ProductDao
import com.example.dto.ProductDto
import com.example.dto.VariantDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.client.RestTemplate
import java.sql.Timestamp

@ExtendWith(MockitoExtension::class)
class ProductServiceTest {

    @Mock
    private lateinit var restTemplate: RestTemplate

    @Mock
    private lateinit var productDAO: ProductDao

    @InjectMocks
    private lateinit var productService: ProductService

    private val testProductId = 1L
    private val testVariantId = 1L
    private lateinit var testProduct: ProductDto
    private lateinit var testVariant: VariantDto
    private lateinit var now: Timestamp

    @BeforeEach
    fun setUp() {
        now = Timestamp(System.currentTimeMillis())
        testVariant = VariantDto(
            id = testVariantId,
            productId = testProductId,
            title = "Test Variant",
            sku = "SKU123",
            price = 100.0,
            available = true,
            option1 = "Option1",
            option2 = "Option2",
            createdAt = now,
            updatedAt = now
        )
        testProduct = ProductDto(
            id = testProductId,
            title = "Test Product",
            vendor = "Test Vendor",
            type = "Test Type",
            variants = listOf(testVariant),
            createdAt = now,
            updatedAt = now
        )
    }



    @Test
    fun `fetchAndSaveProducts should not fetch products when database has entries`() {
        // Arrange
        val mockProduct = mock(ProductDto::class.java)
        `when`(productDAO.count()).thenReturn(1L)

        // Act
        productService.fetchAndSaveProducts()

        // Assert
        verify(productDAO).count()
        verifyNoInteractions(restTemplate)
        verify(productDAO, never()).save(mockProduct)
    }

    @Test
    fun `getAllProducts should return all products`() {
        // Arrange
        val expectedProducts = listOf(testProduct)
        `when`(productDAO.findAll()).thenReturn(expectedProducts)

        // Act
        val result = productService.getAllProducts()

        // Assert
        assertEquals(expectedProducts, result)
        verify(productDAO).findAll()
    }

    @Test
    fun `findAllSortedBy should return sorted products`() {
        // Arrange
        val expectedProducts = listOf(testProduct)
        `when`(productDAO.findAllSortedBy("title", "asc")).thenReturn(expectedProducts)

        // Act
        val result = productService.findAllSortedBy("title", "asc")

        // Assert
        assertEquals(expectedProducts, result)
        verify(productDAO).findAllSortedBy("title", "asc")
    }

    @Test
    fun `saveProduct should save and return product id`() {
        // Arrange
        `when`(productDAO.save(testProduct)).thenReturn(testProductId)

        // Act
        val result = productService.saveProduct(testProduct)

        // Assert
        assertEquals(testProductId, result)
        verify(productDAO).save(testProduct)
    }

    @Test
    fun `updateProduct should update existing product`() {
        // Act
        productService.updateProduct(testProduct, testProductId)

        // Assert
        verify(productDAO).update(testProduct, testProductId)
    }

    @Test
    fun `findByTitleContaining should return matching products`() {
        // Arrange
        val expectedProducts = listOf(testProduct)
        `when`(productDAO.findByTitleContainingIgnoreCase("Test")).thenReturn(expectedProducts)

        // Act
        val result = productService.findByTitleContaining("Test")

        // Assert
        assertEquals(expectedProducts, result)
        verify(productDAO).findByTitleContainingIgnoreCase("Test")
    }

    @Test
    fun `getProductById should return product when exists`() {
        // Arrange
        `when`(productDAO.findById(testProductId)).thenReturn(testProduct)

        // Act
        val result = productService.getProductById(testProductId)

        // Assert
        assertEquals(testProduct, result)
        verify(productDAO).findById(testProductId)
    }

    @Test
    fun `getProductById should throw exception when product not found`() {
        // Arrange
        `when`(productDAO.findById(testProductId)).thenThrow(NoSuchElementException("Product not found"))

        // Act & Assert
        assertThrows(NoSuchElementException::class.java) {
            productService.getProductById(testProductId)
        }
        verify(productDAO).findById(testProductId)
    }

    @Test
    fun `deleteById should delete product`() {
        // Act
        productService.deleteById(testProductId)

        // Assert
        verify(productDAO).deleteById(testProductId)
    }
}