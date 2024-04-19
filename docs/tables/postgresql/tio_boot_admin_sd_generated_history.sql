CREATE TABLE tio_boot_admin_sd_generated_history (
	id BIGINT NOT NULL,
	model VARCHAR ( 32 ),
	mode VARCHAR ( 32 ),
	prompt TEXT NOT NULL,
	output_format VARCHAR ( 4 ),
	negative_prompt TEXT,
	seed VARCHAR ( 10 ),
	aspect_ratio VARCHAR ( 4 ),
	strength VARCHAR ( 4 ),
	src_images json,
	dst_images json,
	remark VARCHAR ( 256 ),
	creator VARCHAR ( 64 ) DEFAULT '',
	create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updater VARCHAR ( 64 ) DEFAULT '',
	update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	deleted SMALLINT NOT NULL DEFAULT 0,
	tenant_id BIGINT NOT NULL DEFAULT 0,
	PRIMARY KEY ( ID ),
	UNIQUE ( update_time )
);

INSERT INTO tio_boot_admin_sd_generated_history (
    id,
    model,
    mode,
    prompt,
    output_format,
    negative_prompt,
    seed,
    aspect_ratio,
    strength,
    src_images,
    dst_images,
    remark,
    creator,
    create_time,
    updater,
    update_time,
    deleted,
    tenant_id
) VALUES (
    1, -- Example ID
    'GPT-4', -- Example model
    'Text2Im', -- Example mode
    'Generate a landscape photo based on description.', -- Example prompt
    'JPEG', -- Example output format
    'No violent content', -- Example negative prompt
    '1234', -- Example seed
    '16:9', -- Example aspect ratio
    'High', -- Example strength
    '[{"url": "http://example.com/image1.jpg", "description": "A beautiful sunset"}]', -- Example JSON array for src_images
    '[{"url": "http://example.com/image2.jpg", "description": "A generated image of a sunset"}]', -- Example JSON array for dst_images
    'Initial test entry', -- Example remark
    'admin', -- Example creator
    CURRENT_TIMESTAMP, -- Automatically filled
    'admin', -- Example updater
    CURRENT_TIMESTAMP, -- Automatically filled
    0, -- Not deleted
    100 -- Example tenant ID
);
