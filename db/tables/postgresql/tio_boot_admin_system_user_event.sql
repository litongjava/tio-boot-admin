CREATE TABLE tio_boot_admin_system_user_event (
  id BIGSERIAL NOT NULL,
  name VARCHAR(255) NOT NULL,
  value JSON NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE tio_boot_admin_system_user_event IS '用户事件表';
COMMENT ON COLUMN tio_boot_admin_system_user_event.id IS 'ID';
COMMENT ON COLUMN tio_boot_admin_system_user_event.name IS 'name';
COMMENT ON COLUMN tio_boot_admin_system_user_event.value IS 'value';
COMMENT ON COLUMN tio_boot_admin_system_user_event.creator IS '创建者';
COMMENT ON COLUMN tio_boot_admin_system_user_event.create_time IS '创建时间';
COMMENT ON COLUMN tio_boot_admin_system_user_event.updater IS '更新者';
COMMENT ON COLUMN tio_boot_admin_system_user_event.update_time IS '更新时间';
COMMENT ON COLUMN tio_boot_admin_system_user_event.deleted IS '是否删除';
COMMENT ON COLUMN tio_boot_admin_system_user_event.tenant_id IS '租户编号';