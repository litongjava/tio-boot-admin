CREATE TABLE tio_boot_admin_system_constants_config (
    id BIGINT NOT NULL,
    key_name VARCHAR(255) NOT NULL,
    key_value VARCHAR(255) NOT NULL,
    remark VARCHAR(256),
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT default 0,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (key_name, update_time)
);

INSERT INTO tio_boot_admin_system_constants_config (
    id, key_name, key_value, creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    1, 'sign.up.user.count', '550', 'admin', '2021-01-05 17:03:47', NULL, '2024-03-23 08:49:55', 0, 1
);
