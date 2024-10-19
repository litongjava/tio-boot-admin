package com.litongjava.tio.boot.admin.costants;

public class TioBootAdminSql {
  public static String getUserById() {
    return "SELECT id,username,nickname,signature,title,group_name,tags,notify_count,unread_count,country,ACCESS,geographic,address,remark,dept_id,post_ids,email,phone,sex,avatar,status,login_ip,login_date,creator,create_time,updater,update_time,tenant_id FROM tio_boot_admin_system_users WHERE id = ? AND deleted = 0";
  }
}
