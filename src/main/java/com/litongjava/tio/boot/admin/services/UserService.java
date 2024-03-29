package com.litongjava.tio.boot.admin.services;

import com.litongjava.jfinal.plugin.activerecord.Db;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.utils.resp.RespVo;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class UserService {
  public RespVo currentUser(Object loginId) {
    String sql = "SELECT id,username,password,nickname,signature,title,group_name,tags," +
      "notify_count,unread_count,country,access,geographic,address,remark,dept_id," +
      "post_ids,email,mobile,sex,avatar,status,login_ip,login_date," +
      "creator,create_time,updater,update_time,tenant_id " +
      "FROM tio_boot_admin_system_users " +
      "where id=? and deleted=0";

    Record first = Db.findFirst(sql, loginId);

    return RespVo.ok(first.toKv());
  }
}
