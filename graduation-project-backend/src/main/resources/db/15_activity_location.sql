USE lvyin_sport;
SET @db = DATABASE();
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='activity' AND column_name='location_lat')=0, 'ALTER TABLE activity ADD COLUMN location_lat DECIMAL(10,7) NULL AFTER venue_id', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='activity' AND column_name='location_lng')=0, 'ALTER TABLE activity ADD COLUMN location_lng DECIMAL(10,7) NULL AFTER location_lat', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='activity' AND column_name='location_address')=0, 'ALTER TABLE activity ADD COLUMN location_address VARCHAR(255) NULL AFTER location_lng', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
