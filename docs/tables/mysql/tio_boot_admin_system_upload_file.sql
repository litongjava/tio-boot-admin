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

INSERT INTO tio_boot_admin_system_upload_file (
  id,
  md5,
  filename,
  file_size,
  user_id,
  platform,
  bucket_name,
  file_id,
  target_name,
  tags,
  creator,
  create_time,
  updater,
  update_time,
  deleted,
  tenant_id
) VALUES (
  1, -- 假设ID为1
  'd41d8cd98f00b204e9800998ecf8427e', -- 示例MD5值
  'example.txt', -- 文件名
  1024, -- 文件大小，单位为字节
  'user123', -- 用户ID
  's3', -- 平台
  'sd', -- 存储桶名称
  '367962274737995776', -- 文件ID
  'public/images/367962274737995776.png', -- 目标名称
  '{"genre": "text", "language": "English"}', -- 标签，使用JSON格式
  'admin', -- 创建者
  CURRENT_TIMESTAMP, -- 创建时间，使用默认值
  'admin', -- 更新者
  CURRENT_TIMESTAMP, -- 更新时间，使用默认值
  0, -- 删除标志
  100 -- 租户ID
);
