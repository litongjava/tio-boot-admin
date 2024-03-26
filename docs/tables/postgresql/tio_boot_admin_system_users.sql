CREATE TABLE tio_boot_admin_system_users (
  id BIGINT NOT NULL,
  username VARCHAR(30) NOT NULL,
  password VARCHAR(100) NOT NULL DEFAULT '',
  nickname VARCHAR(30) NOT NULL,
  remark VARCHAR(500),
  dept_id BIGINT,
  post_ids VARCHAR(255),
  email VARCHAR(50) DEFAULT '',
  mobile VARCHAR(11) DEFAULT '',
  sex SMALLINT DEFAULT 0,
  avatar VARCHAR(512) DEFAULT '',
  status SMALLINT NOT NULL DEFAULT 0,
  login_ip VARCHAR(50) DEFAULT '',
  login_date TIMESTAMP WITHOUT TIME ZONE,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE (username, update_time, tenant_id)
);

COMMENT ON COLUMN tio_boot_admin_system_users.id IS '用户ID';
COMMENT ON COLUMN tio_boot_admin_system_users.username IS '用户账号';
COMMENT ON COLUMN tio_boot_admin_system_users.password IS '密码';
COMMENT ON COLUMN tio_boot_admin_system_users.nickname IS '用户昵称';
COMMENT ON COLUMN tio_boot_admin_system_users.remark IS '备注';
COMMENT ON COLUMN tio_boot_admin_system_users.dept_id IS '部门ID';
COMMENT ON COLUMN tio_boot_admin_system_users.post_ids IS '岗位编号数组';
COMMENT ON COLUMN tio_boot_admin_system_users.email IS '用户邮箱';
COMMENT ON COLUMN tio_boot_admin_system_users.mobile IS '手机号码';
COMMENT ON COLUMN tio_boot_admin_system_users.sex IS '用户性别';
COMMENT ON COLUMN tio_boot_admin_system_users.avatar IS '头像地址';
COMMENT ON COLUMN tio_boot_admin_system_users.status IS '帐号状态（0正常 1停用）';
COMMENT ON COLUMN tio_boot_admin_system_users.login_ip IS '最后登录IP';
COMMENT ON COLUMN tio_boot_admin_system_users.login_date IS '最后登录时间';
COMMENT ON COLUMN tio_boot_admin_system_users.creator IS '创建者';
COMMENT ON COLUMN tio_boot_admin_system_users.create_time IS '创建时间';
COMMENT ON COLUMN tio_boot_admin_system_users.updater IS '更新者';
COMMENT ON COLUMN tio_boot_admin_system_users.update_time IS '更新时间';
COMMENT ON COLUMN tio_boot_admin_system_users.deleted IS '是否删除';
COMMENT ON COLUMN tio_boot_admin_system_users.tenant_id IS '租户编号';

INSERT INTO tio_boot_admin_system_users VALUES (1, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'admin', '管理员', 103, '[1]', 'aoteman@126.com', '15612345678', 1, 'http://127.0.0.1:48080/admin-api/infra/file/4/get/37e56010ecbee472cdd821ac4b608e151e62a74d9633f15d085aee026eedeb60.png', 0, '127.0.0.1', '2023-11-30 09:16:00', 'admin', '2021-01-05 17:03:47', NULL, '2024-03-23 08:49:55', FALSE, 1);
