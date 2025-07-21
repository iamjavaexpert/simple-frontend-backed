package com.example.controller

import com.example.dto.ProductDto
import com.example.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

/**
 * Controller responsible for handling product-related web requests.
 */
@Controller
class ProductController(
    private val productService: ProductService
) {

    /** Displays the home page. */
    @GetMapping("/")
    fun index(): String = "index"

    /** Displays sorted product table fragment. */
    @GetMapping("/products/table")
    fun getProducts(
        @RequestParam(defaultValue = "updated_at") sort: String,
        @RequestParam(defaultValue = "desc") direction: String,
        model: Model
    ): String {
        model.addAttribute("products", productService.findAllSortedBy(sort, direction))
        model.addAttribute("sort", sort)
        model.addAttribute("direction", direction)
        return "fragments/product-table :: products-table"
    }

    /** Displays all products using Thymeleaf fragment. */
    @GetMapping("/products")
    fun getAllProducts(model: Model): String {
        model.addAttribute("products", productService.getAllProducts())
        return "fragments/products :: products"
    }

    /** Refreshes and returns product table fragment. */
    @GetMapping("/products/refresh")
    fun refreshProductTable(model: Model): String {
        model.addAttribute("products", productService.getAllProducts())
        return "fragments/product-table :: products-table"
    }

    /** Saves a new product and returns updated table. */
    @PostMapping("/product")
    fun addProduct(@ModelAttribute productDto: ProductDto, model: Model): String {
        productService.saveProduct(productDto)
        model.addAttribute("products", productService.getAllProducts())
        return "fragments/product-table :: products-table"
    }

    /** Searches products by title and returns result as a table fragment. */
    @GetMapping("/product/search/table")
    fun searchProducts(@ModelAttribute("title") title: String, model: Model): String {
        model.addAttribute("products", productService.findByTitleContaining(title))
        return "fragments/product-table :: products-table"
    }

    /** Displays the product search form. */
    @GetMapping("/products/search")
    fun showSearchPage(model: Model): String {
        model.addAttribute("products", productService.getAllProducts())
        return "fragments/search-product :: search-product"
    }

    /** Loads the product edit form for a given ID. */
    @GetMapping("/products/edit/{id}")
    fun editProduct(@PathVariable id: Long, model: Model): String {
        model.addAttribute("product", productService.getProductById(id))
        return "fragments/edit-product :: edit-product-form"
    }

    /** Updates product and variants, then returns updated product list. */
    @PutMapping("/products/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @ModelAttribute productForm: ProductDto,
        model: Model
    ): String {
        productService.updateProduct(productForm, id)
        model.addAttribute("products", productService.getAllProducts())
        return "fragments/products :: products"
    }

    /** Deletes a product by ID and returns HTTP 200 OK. */
    @DeleteMapping("/products/{id}")
    @ResponseBody
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        productService.deleteById(id)
        return ResponseEntity.ok().build()
    }
}
