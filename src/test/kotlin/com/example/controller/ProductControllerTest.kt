package com.example.controller

import com.example.dto.ProductDto
import com.example.service.ProductService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.sql.Timestamp

@WebMvcTest(ProductController::class)
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var productService: ProductService

    private fun sampleProducts(): List<ProductDto> = listOf(
        ProductDto(
            id = 1L,
            title = "Product 1",
            vendor = "Vendor A",
            type = "Type A",
            variants = emptyList(),
            createdAt = Timestamp.valueOf("2024-01-01 00:00:00"),
            updatedAt = Timestamp.valueOf("2024-01-02 00:00:00")
        ),
        ProductDto(
            id = 2L,
            title = "Product 2",
            vendor = "Vendor B",
            type = "Type B",
            variants = emptyList(),
            createdAt = Timestamp.valueOf("2024-01-03 00:00:00"),
            updatedAt = Timestamp.valueOf("2024-01-04 00:00:00")
        )
    )

    @Test
    fun `should return product fragment when fetching sorted products`() {
        `when`(productService.findAllSortedBy("updated_at", "desc")).thenReturn(sampleProducts())

        mockMvc.perform(get("/products").param("sort", "updated_at").param("direction", "desc"))
            .andExpect(status().isOk)
            .andExpect(view().name("fragments/products :: products"))
            .andExpect(model().attributeExists("products"))
    }

    @Test
    fun `should return product table fragment on refresh`() {
        `when`(productService.getAllProducts()).thenReturn(sampleProducts())

        mockMvc.perform(get("/products/refresh"))
            .andExpect(status().isOk)
            .andExpect(view().name("fragments/product-table :: products-table"))
            .andExpect(model().attributeExists("products"))
    }

    @Test
    fun `should save product and return updated product table`() {
        val productDto = ProductDto(
            title = "Product 3",
            vendor = "Vendor C",
            type = "Type C",
            variants = emptyList()
        )

        `when`(productService.saveProduct(productDto)).thenReturn(3L)
        `when`(productService.getAllProducts()).thenReturn(sampleProducts())

        mockMvc.perform(
            post("/product")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", productDto.title)
                .param("vendor", productDto.vendor)
                .param("type", productDto.type)
        )
            .andExpect(status().isOk)
            .andExpect(view().name("fragments/product-table :: products-table"))
            .andExpect(model().attributeExists("products"))
    }

    @Test
    fun `should return filtered product table when searching by title`() {
        val filteredProducts = listOf(sampleProducts()[0])
        `when`(productService.findByTitleContaining("Product 1")).thenReturn(filteredProducts)

        mockMvc.perform(get("/product/search/table").param("title", "Product 1"))
            .andExpect(status().isOk)
            .andExpect(view().name("fragments/product-table :: products-table"))
            .andExpect(model().attributeExists("products"))
    }

    @Test
    fun `should return search page with all products`() {
        `when`(productService.getAllProducts()).thenReturn(sampleProducts())

        mockMvc.perform(get("/products/search"))
            .andExpect(status().isOk)
            .andExpect(view().name("fragments/search-product :: search-product"))
            .andExpect(model().attributeExists("products"))
    }

    @Test
    fun `should return edit-product form for a given product`() {
        val product = sampleProducts()[0]
        `when`(productService.getProductById(1L)).thenReturn(product)

        mockMvc.perform(get("/products/edit/1"))
            .andExpect(status().isOk)
            .andExpect(view().name("fragments/edit-product :: edit-product-form"))
            .andExpect(model().attribute("product", product))
    }

    @Test
    fun `should update product and return updated products list`() {
        val updatedProduct = ProductDto(
            id = 1L,
            title = "Updated Product",
            vendor = "Vendor A",
            type = "Type A",
            variants = emptyList()
        )

        `when`(productService.getAllProducts()).thenReturn(sampleProducts())

        mockMvc.perform(
            put("/products/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", updatedProduct.title)
                .param("vendor", updatedProduct.vendor)
                .param("type", updatedProduct.type)
        )
            .andExpect(status().isOk)
            .andExpect(view().name("fragments/products :: products"))
            .andExpect(model().attributeExists("products"))
    }

    @Test
    fun `should delete product and return 200 OK`() {
        mockMvc.perform(delete("/products/1"))
            .andExpect(status().isOk)
    }
}
