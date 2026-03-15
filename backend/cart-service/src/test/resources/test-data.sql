TRUNCATE TABLE carts CASCADE;

INSERT INTO carts (id, user_id, status, created_at, updated_at, version)
VALUES
    (1, 'user-1', 'ACTIVE', NOW(), NOW(), 0),
    (2, 'user-2', 'ACTIVE', NOW(), NOW(), 0);


INSERT INTO cart_items (id,cart_id, product_code, product_name, quantity, image_url, price, created_at, updated_at)
VALUES
    (101,1, 'B001', 'The Great Gatsby', 2, 'https://mybookstore-coverimages-2026.s3.eu-west-2.amazonaws.com/Coverpage/B001.jpg', 10.00, NOW(), NOW()),
    (102, 1, 'B003', 'To Kill a Mockingbird', 1, 'https://mybookstore-coverimages-2026.s3.eu-west-2.amazonaws.com/Coverpage/B003.jpg', 15.00, NOW(), NOW());