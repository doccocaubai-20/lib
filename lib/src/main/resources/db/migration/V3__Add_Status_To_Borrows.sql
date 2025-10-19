-- V3__Add_Status_To_Borrows.sql
ALTER TABLE borrows
ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER `return_date`;