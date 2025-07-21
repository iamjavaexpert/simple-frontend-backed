-- Create the 'products' table to store product details
CREATE TABLE products
(
    id         BIGINT PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    vendor     VARCHAR(255),
    type       VARCHAR(255),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

-- Create the 'variants' table to store variants of each product
CREATE TABLE variants
(
    id         BIGINT PRIMARY KEY,
    product_id BIGINT         NOT NULL,
    title      VARCHAR(255)   NOT NULL,
    sku        VARCHAR(255),
    price      DECIMAL(10, 2) NOT NULL,
    available  BOOLEAN        NOT NULL,
    option1    VARCHAR(255),
    option2    VARCHAR(255),
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products (id)
);