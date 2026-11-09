USE lvyin_sport;

-- Activity cover image. Existing activities remain valid and continue using the sport default image.
SET @has_image_url := (SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'activity' AND column_name = 'image_url');
SET @add_image_url := IF(@has_image_url = 0,
  'ALTER TABLE activity ADD COLUMN image_url MEDIUMTEXT NULL AFTER description',
  'SELECT 1');
PREPARE activity_image_stmt FROM @add_image_url;
EXECUTE activity_image_stmt;
DEALLOCATE PREPARE activity_image_stmt;
