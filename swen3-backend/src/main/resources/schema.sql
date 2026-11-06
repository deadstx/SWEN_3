-- Allow RustFS-backed uploads on existing databases without deleting legacy PDFs.
-- On a fresh database Hibernate creates the table afterwards.
ALTER TABLE IF EXISTS documents ALTER COLUMN content DROP NOT NULL;
