
-- Bảng roles
CREATE TABLE `roles` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `roles_name_unique` (`name`)
);

-- Bảng users
CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(255),
  `is_active` BOOLEAN DEFAULT TRUE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_username_unique` (`username`)
);

-- Bảng user_roles (bảng trung gian)
CREATE TABLE `user_roles` (
  `user_id` BIGINT UNSIGNED NOT NULL,
  `role_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
);

-- Bảng authors
CREATE TABLE `authors` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`)
);

-- Bảng categories
CREATE TABLE `categories` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`)
);

-- Bảng books
CREATE TABLE `books` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `author_id` BIGINT UNSIGNED,
  `category_id` BIGINT UNSIGNED,
  `publish_date` DATE,
  `quantity` INT UNSIGNED NOT NULL DEFAULT 0,
  `pdf_path` VARCHAR(255),
  `description` LONGTEXT, -- <<< ĐÃ SỬA TỪ TEXT THÀNH LONGTEXT
  `image` VARCHAR(255),
  `language` VARCHAR(10),
  PRIMARY KEY (`id`),
  FOREIGN KEY (`author_id`) REFERENCES `authors` (`id`),
  FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
);

-- Bảng reviews
CREATE TABLE `reviews` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `book_id` BIGINT UNSIGNED NOT NULL,
    `rating` INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    `comment` LONGTEXT, -- <<< ĐÃ SỬA TỪ TEXT THÀNH LONGTEXT
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE
);

-- Bảng borrows
CREATE TABLE `borrows` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `book_id` BIGINT UNSIGNED NOT NULL,
  `borrow_date` DATE,
  `return_date` DATE,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  FOREIGN KEY (`book_id`) REFERENCES `books` (`id`)
);

-- Thêm các vai trò mặc định
INSERT INTO `roles` (`name`) VALUES ('ROLE_USER'), ('ROLE_ADMIN');