CREATE TABLE tio_boot_admin_system_pdf (
  id BIGINT NOT NULL,
  name VARCHAR ( 256 ),
  version VARCHAR ( 32 ),
  urls json,
  remark VARCHAR ( 256 ),
  creator VARCHAR ( 64 ) DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR ( 64 ) DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted SMALLINT NOT NULL DEFAULT 0,
  tenant_id BIGINT NOT NULL DEFAULT 0,
PRIMARY KEY ( id ) 
);