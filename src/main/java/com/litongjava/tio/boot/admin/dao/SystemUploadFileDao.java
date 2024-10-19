package com.litongjava.tio.boot.admin.dao;

import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Record;

public class SystemUploadFileDao {
  public Record getFileBasicInfoByMd5(String md5) {
    String sql = "select id,filename,bucket_name,target_name from tio_boot_admin_system_upload_file where md5=? and deleted=0";
    return Db.findFirst(sql, md5);
  }

  public Record getFileBasicInfoById(long id) {
    String sql = "select md5,filename,bucket_name,target_name from tio_boot_admin_system_upload_file where id=? and deleted=0";
    return Db.findFirst(sql, id);

  }
}
