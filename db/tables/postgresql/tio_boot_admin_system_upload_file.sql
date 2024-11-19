CREATE TABLE tio_boot_admin_system_upload_file (
  id BIGINT NOT NULL,
  md5 varchar NOT NULL,
  name VARCHAR NOT NULL,
  size BIGINT NOT NULL,
  user_id VARCHAR,
  platform VARCHAR NOT NULL,
  region_name VARCHAR,
  bucket_name VARCHAR NOT NULL,
  file_id VARCHAR NOT NULL,
  target_name VARCHAR NOT NULL,
  tags JSON,
  creator VARCHAR DEFAULT '',
  create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR DEFAULT '',
  update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted SMALLINT  NOT NULL DEFAULT 0,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
)

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
  'sd',--bucket_name
  '367962274737995776', -- 文件ID
  'public/images/367962274737995776.png', -- target-name
  '{"genre": "text", "language": "English"}', -- 标签，使用JSON格式
  'admin', -- 创建者
  CURRENT_TIMESTAMP, -- 创建时间，使用默认值
  'admin', -- 更新者
  CURRENT_TIMESTAMP, -- 更新时间，使用默认值
  0, -- deleted标志
  100 -- 租户ID
);
