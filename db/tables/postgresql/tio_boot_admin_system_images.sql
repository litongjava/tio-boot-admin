CREATE TABLE tio_boot_admin_system_images (
  id BIGINT NOT NULL,
  name VARCHAR ( 256 ),
  category VARCHAR ( 256 ),
  urls json,
  remark VARCHAR(256),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted SMALLINT default 0,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
);