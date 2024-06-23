-- ----------------------------
-- Table structure for tio_boot_admin_android_app_version
-- ----------------------------
DROP TABLE IF EXISTS `tio_boot_admin_android_app_version`;
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
  deleted SMALLINT NOT NULL DEFAULT 0,
  tenant_id BIGINT NOT NULL DEFAULT 0,
PRIMARY KEY ( id ) 
);

-- ----------------------------
-- Table structure for tio_boot_admin_hardware_app_version
-- ----------------------------
DROP TABLE IF EXISTS `tio_boot_admin_hardware_app_version`;
CREATE TABLE tio_boot_admin_hardware_app_version (
  id BIGINT NOT NULL,
  version FLOAT NOT NULL UNIQUE,
  `urls` json DEFAULT NULL,
  is_must_update TINYINT,
  remark VARCHAR ( 256 ),
  creator VARCHAR ( 64 ) DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR ( 64 ) DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted SMALLINT NOT NULL DEFAULT 0,
  tenant_id BIGINT NOT NULL DEFAULT 0,
PRIMARY KEY ( id ) 
);

-- ----------------------------
-- Table structure for tio_boot_admin_system_article
-- ----------------------------
DROP TABLE IF EXISTS `tio_boot_admin_system_article`;
CREATE TABLE tio_boot_admin_system_article (
    id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content Text NOT NULL,
    remark VARCHAR(256),
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY `unique_key_update_time` (title, update_time)
);


-- ----------------------------
-- Table structure for tio_boot_admin_system_constants_config
-- ----------------------------
DROP TABLE IF EXISTS `tio_boot_admin_system_constants_config`;
CREATE TABLE tio_boot_admin_system_constants_config (
    id BIGINT NOT NULL,
    key_name VARCHAR(255) NOT NULL,
    key_value Text NOT NULL,
    remark VARCHAR(256),
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY `unique_key_update_time` (key_name, update_time)
);

INSERT INTO tio_boot_admin_system_constants_config (
    id, key_name, key_value, creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    1, 'sign.up.user.count', '550', 'admin', '2021-01-05 17:03:47', NULL, '2024-03-23 08:49:55', 0, 1
);


-- ----------------------------
-- Table structure for tio_boot_admin_system_docx
-- ----------------------------
DROP TABLE IF EXISTS `tio_boot_admin_system_docx`;
CREATE TABLE tio_boot_admin_system_docx (
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

-- ----------------------------
-- Table structure for tio_boot_admin_system_pdf
-- ----------------------------
DROP TABLE IF EXISTS `tio_boot_admin_system_pdf`;
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

-- ----------------------------
-- Table structure for tio_boot_admin_system_upload_file
-- ----------------------------
DROP TABLE IF EXISTS `tio_boot_admin_system_upload_file`;
CREATE TABLE tio_boot_admin_system_upload_file (
  id BIGINT NOT NULL,
  md5 VARCHAR(32) NOT NULL,
  filename VARCHAR(64) NOT NULL,
  file_size BIGINT NOT NULL,
  user_id VARCHAR(32),
  platform VARCHAR(64) NOT NULL,
  region_name VARCHAR(32),
  bucket_name VARCHAR(64) NOT NULL,
  file_id VARCHAR(64) NOT NULL,
  target_name VARCHAR(64) NOT NULL,
  tags JSON,
  creator VARCHAR(64) DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted SMALLINT NOT NULL DEFAULT 0,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);
-- ----------------------------
-- Table structure for tio_boot_admin_system_urls
-- ----------------------------
DROP TABLE IF EXISTS `tio_boot_admin_system_urls`;
CREATE TABLE tio_boot_admin_system_urls (
    id BIGINT NOT NULL,
    key_name VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    remark VARCHAR(256),
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY `unique_key_update_time` (key_name, name)
);

-- ----------------------------
-- Table structure for tio_boot_admin_system_users
-- ----------------------------
DROP TABLE IF EXISTS `tio_boot_admin_system_users`;
CREATE TABLE tio_boot_admin_system_users (
    id BIGINT NOT NULL,
    username VARCHAR(30) NOT NULL,
    password VARCHAR(100) NOT NULL DEFAULT '',
    nickname VARCHAR(30) NOT NULL,
    signature VARCHAR(200),
    title VARCHAR(50),
    group_name VARCHAR(50),
    tags JSON,
    notify_count INT DEFAULT 0,
    unread_count INT DEFAULT 0,
    country VARCHAR(50),
    access VARCHAR(20),
    geographic JSON,
    address VARCHAR(200),
    remark VARCHAR(500),
    dept_id BIGINT,
    post_ids VARCHAR(255),
    email VARCHAR(50) DEFAULT '',
    phone VARCHAR(11) DEFAULT '',
    sex SMALLINT DEFAULT 0,
    avatar VARCHAR(512) DEFAULT '',
    status SMALLINT NOT NULL DEFAULT 0,
    login_ip VARCHAR(50) DEFAULT '',
    login_date DATETIME,
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY `unique_username_updatetime` (username, update_time)
);
