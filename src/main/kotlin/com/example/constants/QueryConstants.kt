package com.example.constants

/**
 * A utility object that contains SQL query constants used for database operations.
 * These queries handle CRUD operations for products and variants tables.
 *
 * All queries are organized by operation type and use named parameters for security and readability.
 */
object QueryConstants {

    // ========== PRODUCT QUERIES ==========

    /**
     * Retrieves all products from the products table.
     */
    const val GET_ALL_PRODUCTS = """
        SELECT id, title, vendor, type, created_at, updated_at 
        FROM products order by updated_at desc
    """

    /**
     * Finds a specific product by its ID.
     */
    const val FIND_PRODUCT_BY_ID = """
        SELECT id, title, vendor, type, created_at, updated_at 
        FROM products 
        WHERE id = :id
    """

    /**
     * Searches for products by title using case-insensitive partial matching.
     */
    const val FIND_PRODUCTS_BY_TITLE_LIKE = """
        SELECT id, title, vendor, type, created_at, updated_at 
        FROM products 
        WHERE LOWER(title) LIKE LOWER(:title)
    """

    /**
     * Retrieves products sorted by a specified field and direction.
     * Note: sortField and direction should be validated before use to prevent SQL injection.
     */
    const val GET_PRODUCTS_SORTED = """
        SELECT id, title, vendor, type, created_at, updated_at 
        FROM products 
        ORDER BY {sortField} {direction}
    """

    /**
     * Inserts a new product into the products table.
     */
    const val SAVE_PRODUCT = """
        INSERT INTO products (id, title, vendor, type, created_at, updated_at)
        VALUES (:id, :title, :vendor, :type, :created_at, :updated_at)
    """

    /**
     * Updates an existing product's basic information.
     */
    const val UPDATE_PRODUCT = """
        UPDATE products 
        SET title = :title, 
            vendor = :vendor, 
            type = :type, 
            updated_at = :updated_at
        WHERE id = :id
    """

    /**
     * Deletes a product by its ID.
     */
    const val DELETE_PRODUCT_BY_ID = """
        DELETE FROM products 
        WHERE id = :id
    """

    /**
     * Checks if a product exists by ID.
     */
    const val EXISTS_PRODUCT_BY_ID = """
        SELECT 1 FROM products 
        WHERE id = :id 
        LIMIT 1
    """

    /**
     * Returns the total count of products in the database.
     */
    const val PRODUCT_COUNT = """
        SELECT COUNT(*) FROM products
    """

    // ========== VARIANT QUERIES ==========

    /**
     * Retrieves all variants for a specific product by product ID.
     */
    const val GET_ALL_VARIANTS_OF_A_PRODUCT = """
        SELECT id, product_id, title, sku, price, available, 
               option1, option2, created_at, updated_at
        FROM variants 
        WHERE product_id = :id
    """

    /**
     * Retrieves all variant IDs for a specific product.
     * Used for cleanup operations during updates.
     */
    const val GET_VARIANT_IDS_BY_PRODUCT_ID = """
        SELECT id 
        FROM variants 
        WHERE product_id = :product_id
    """

    /**
     * Finds a specific variant by its ID.
     */
    const val FIND_VARIANT_BY_ID = """
        SELECT id, product_id, title, sku, price, available, 
               option1, option2, created_at, updated_at
        FROM variants 
        WHERE id = :id
    """

    /**
     * Inserts a new variant into the variants table.
     */
    const val SAVE_VARIANT = """
        INSERT INTO variants (
            id, product_id, title, sku, price, available,
            option1, option2, created_at, updated_at
        )
        VALUES (
            :id, :product_id, :title, :sku, :price, :available, 
            :option1, :option2, :created_at, :updated_at
        )
    """

    /**
     * Updates an existing variant's information.
     */
    const val UPDATE_VARIANT = """
        UPDATE variants 
        SET title = :title, 
            sku = :sku, 
            price = :price, 
            option1 = :option1, 
            option2 = :option2, 
            available = :available, 
            updated_at = :updated_at
        WHERE id = :id AND product_id = :product_id
    """

    /**
     * Deletes all variants associated with a specific product.
     * Used when deleting a product to maintain referential integrity.
     */
    const val DELETE_VARIANTS_BY_PRODUCT_ID = """
        DELETE FROM variants 
        WHERE product_id = :id
    """

    /**
     * Deletes a specific variant by its ID.
     */
    const val DELETE_VARIANT_BY_ID = """
        DELETE FROM variants 
        WHERE id = :id
    """

    /**
     * Deletes multiple variants by their IDs.
     * Used for cleanup during product updates.
     */
    const val DELETE_VARIANTS_BY_IDS = """
        DELETE FROM variants 
        WHERE id IN (:ids)
    """

    /**
     * Returns the total count of variants in the database.
     */
    const val VARIANT_COUNT = """
        SELECT COUNT(*) FROM variants
    """

    /**
     * Returns the count of variants for a specific product.
     */
    const val VARIANT_COUNT_BY_PRODUCT = """
        SELECT COUNT(*) FROM variants 
        WHERE product_id = :product_id
    """

    // ========== BATCH OPERATIONS ==========

    /**
     * Template for batch insert operations on products.
     * Used when inserting multiple products at once.
     */
    const val BATCH_INSERT_PRODUCTS = """
        INSERT INTO products (id, title, vendor, type, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?)
    """

    /**
     * Template for batch insert operations on variants.
     * Used when inserting multiple variants at once.
     */
    const val BATCH_INSERT_VARIANTS = """
        INSERT INTO variants (
            id, product_id, title, sku, price, available,
            option1, option2, created_at, updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    // ========== UTILITY QUERIES ==========

    /**
     * Retrieves the maximum ID from products table.
     * Can be used for ID generation strategies.
     */
    const val GET_MAX_PRODUCT_ID = """
        SELECT COALESCE(MAX(id), 0) FROM products
    """

    /**
     * Retrieves the maximum ID from variants table.
     * Can be used for ID generation strategies.
     */
    const val GET_MAX_VARIANT_ID = """
        SELECT COALESCE(MAX(id), 0) FROM variants
    """

    /**
     * Validates that a product exists before performing variant operations.
     */
    const val VALIDATE_PRODUCT_EXISTS = """
        SELECT EXISTS(SELECT 1 FROM products WHERE id = :product_id)
    """
}