CREATE SEQUENCE carts_id_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE cart_items_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE carts (
                 id BIGINT DEFAULT nextval('carts_id_seq') NOT NULL,
                 user_id TEXT  NOT NULL,
                 status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CHECKED_OUT', 'ABANDONED')),
                 created_at TIMESTAMP NOT NULL,
                 updated_at TIMESTAMP,
                 version BIGINT NOT NULL DEFAULT 0,
                 PRIMARY KEY (id)
);


CREATE TABLE cart_items (
                 id BIGINT DEFAULT nextval('cart_items_id_seq') NOT NULL,
                 cart_id BIGINT NOT NULL,
                 product_code TEXT NOT NULL,
                 product_name TEXT NOT NULL,
                 quantity INTEGER NOT NULL CHECK (quantity >= 1),
                 price NUMERIC NOT NULL CHECK (price >= 0.01),
                 created_at TIMESTAMP NOT NULL,
                 updated_at TIMESTAMP,
                 version BIGINT NOT NULL DEFAULT 0,
                 PRIMARY KEY (id),
                 FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
                 UNIQUE (cart_id, product_code)
);
