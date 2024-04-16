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
    login_date TIMESTAMP WITHOUT TIME ZONE,
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT  NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (username, update_time)
);

INSERT INTO tio_boot_admin_system_users (
    id, username, password, nickname, signature, title, group_name, tags, notify_count, unread_count, country, access, geographic, address, remark, dept_id, post_ids, email, phone, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    1, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'admin', 'This is a signature', 'Admin', 'Administrators', '{"tags": [{"key": "tag1", "label": "Tag 1"}, {"key": "tag2", "label": "Tag 2"}]}', 10, 5, 'United States', 'admin', '{"province": {"label": "California", "key": "CA"}, "city": {"label": "San Francisco", "key": "SF"}}', '123 Main St, San Francisco, CA 94122', '管理员', 103, '[1]', 'aoteman@126.com', '15612345678', 1, 'http://127.0.0.1:48080/admin-api/infra/file/4/get/37e56010ecbee472cdd821ac4b608e151e62a74d9633f15d085aee026eedeb60.png', 0, '127.0.0.1', '2023-11-30 09:16:00', 'admin', '2021-01-05 17:03:47', NULL, '2024-03-23 08:49:55', 0, 1
);
