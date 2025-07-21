CREATE TABLE products (
                          id BIGINT PRIMARY KEY,
                          title VARCHAR(255),
                          vendor VARCHAR(255),
                          type VARCHAR(255),
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP
);

CREATE TABLE variants (
                          id BIGINT PRIMARY KEY,
                          product_id BIGINT,
                          title VARCHAR(255),
                          sku VARCHAR(255),
                          price DOUBLE,
                          available BOOLEAN,
                          option1 VARCHAR(255),
                          option2 VARCHAR(255),
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          FOREIGN KEY (product_id) REFERENCES products(id)
);
