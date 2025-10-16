-- V2__Add_Page_Count_To_Books.sql
ALTER TABLE books
ADD COLUMN `page_count` INT UNSIGNED NULL AFTER `publish_date`;