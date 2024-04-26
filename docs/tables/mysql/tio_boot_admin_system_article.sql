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
