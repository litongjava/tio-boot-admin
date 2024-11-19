package com.litongjava.tio.boot.admin.dao;

import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Record;

public class SystemUploadFileDao {
  public static final String tableName = "tio_boot_admin_system_upload_file";
  public static final String getFileBasicInfoByMd5Sql = String.format(
      //
      "select id,name,size,bucket_name,target_name from %s  where md5=? and deleted=0", tableName);

  public static final String getFileBasicInfoByIdSql = String.format(
      //
      "select md5,name,size,bucket_name,target_name from %s where id=? and deleted=0", tableName);

  public Record getFileBasicInfoByMd5(String md5) {
    return Db.findFirst(getFileBasicInfoByMd5Sql, md5);
  }

  public Record getFileBasicInfoById(long id) {
    return Db.findFirst(getFileBasicInfoByIdSql, id);
  }

  public boolean save(long id, String md5, String originname, int fileSize, String platform, String bucketName,
      //
      String targetName) {
    Record record = Record.by("id", id)
        //
        .set("md5", md5).set("name", originname).set("size", fileSize)
        //
        .set("platform", platform).set("bucket_name", bucketName)
        //
        .set("target_name", targetName);

    return Db.save(tableName, record);
  }
}
