#namespace("user")
  #sql("adminUser")
    SELECT ID
    	id,
    	username,
    	nickname,
    	signature,
    	title,
    	group_name,
    	tags,
    	notify_count,
    	unread_count,
    	country,
    	ACCESS,
    	geographic,
    	address,
    	remark,
    	dept_id,
    	post_ids,
    	email,
    	mobile,
    	sex,
    	avatar,
    	status,
    	login_ip,
    	login_date,
    	creator,
    	create_time,
    	updater,
    	update_time,
    	tenant_id
    FROM
    	tio_boot_admin_system_users
    WHERE
    	ID = 1
    	AND deleted = 0
  #end
  #sql("getUserById")
    SELECT ID
    	id,
    	username,
    	nickname,
    	signature,
    	title,
    	group_name,
    	tags,
    	notify_count,
    	unread_count,
    	country,
    	ACCESS,
    	geographic,
    	address,
    	remark,
    	dept_id,
    	post_ids,
    	email,
    	phone,
    	sex,
    	avatar,
    	status,
    	login_ip,
    	login_date,
    	creator,
    	create_time,
    	updater,
    	update_time,
    	tenant_id
    FROM
    	tio_boot_admin_system_users
    WHERE
    	ID = ?
    	AND deleted = 0
  #end
#end

