CREATE SEQUENCE products_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE products (
                          id BIGINT DEFAULT nextval('products_id_seq') NOT NULL,
                          code TEXT NOT NULL UNIQUE,
                          name TEXT NOT NULL,
                          author TEXT NOT NULL,
                          isbn TEXT,
                          description TEXT,
                          image_url TEXT,
                          price NUMERIC NOT NULL CHECK (price >= 0.01),
                          genre TEXT,
                          publisher TEXT,
                          publication_year INTEGER,
                          stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
                          status TEXT NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED')),
                          created_at TIMESTAMP NOT NULL ,
                          updated_at TIMESTAMP,
                          version BIGINT NOT NULL DEFAULT 0, -- Version field for optimistic locking
                          PRIMARY KEY (id)
);