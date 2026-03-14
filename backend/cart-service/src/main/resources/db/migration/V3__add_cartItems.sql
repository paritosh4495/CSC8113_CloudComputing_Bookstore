INSERT INTO cart_items (cart_id, product_code, product_name, quantity, image_url, price, created_at, updated_at)
VALUES
    (1, 'B001', 'The Great Gatsby', 2, 'https://mybookstore-coverimages-2026.s3.eu-west-2.amazonaws.com/Coverpage/B004.jpg', 10.99, NOW(), NOW()),
    (1, 'B003', 'To Kill a Mockingbird', 1, 'https://mybookstore-coverimages-2026.s3.eu-west-2.amazonaws.com/Coverpage/B002.jpg', 14.99, NOW(), NOW());

INSERT INTO cart_items (cart_id, product_code, product_name, quantity, image_url, price, created_at, updated_at)
VALUES
    (51, 'B007', 'The Hobbit', 1, 'https://mybookstore-coverimages-2026.s3.eu-west-2.amazonaws.com/Coverpage/B007.jpg', 18.00, NOW(), NOW()),
    (51, 'B010', 'The Hunger Games', 3, 'https://mybookstore-coverimages-2026.s3.eu-west-2.amazonaws.com/Coverpage/B010.jpg', 10.49, NOW(), NOW());