USE lvyin_sport;

ALTER TABLE `user` ADD COLUMN email VARCHAR(254) CHARACTER SET ascii COLLATE ascii_general_ci NULL AFTER phone;
CREATE UNIQUE INDEX uk_user_phone ON `user` (phone);
CREATE UNIQUE INDEX uk_user_email ON `user` (email);
