DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name='labels'
        AND column_name='label_name'
    ) THEN
        ALTER TABLE labels
        RENAME COLUMN label_name TO name;
    END IF;
END $$;