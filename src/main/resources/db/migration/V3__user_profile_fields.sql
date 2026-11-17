ALTER TABLE users
    MODIFY COLUMN display_name VARCHAR(50) NULL,
    MODIFY COLUMN email VARCHAR(50) NULL,
    ADD COLUMN contact_number VARCHAR(10) NULL AFTER display_name,
    ADD COLUMN external_role VARCHAR(50) NULL AFTER picture_url;
