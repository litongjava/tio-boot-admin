CREATE TABLE tio_boot_admin_android_app_version (
  id BIGINT NOT NULL,
  version FLOAT NOT NULL UNIQUE,
  `urls` json DEFAULT NULL,
  is_must_update TINYINT,
  remark VARCHAR ( 256 ),
  creator VARCHAR ( 64 ) DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR ( 64 ) DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted SMALLINT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
PRIMARY KEY ( id ) 
);