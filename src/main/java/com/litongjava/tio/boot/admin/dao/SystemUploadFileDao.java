package com.litongjava.tio.boot.admin.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;

public class SystemUploadFileDao {
  public static final String tableName = "tio_boot_admin_system_upload_file";
  private static final String selectSqlByMd5 = "select id,name,size,platform,region_name,bucket_name,target_name from %s  where md5=? and deleted=0";

  public static final String getFileBasicInfoByMd5Sql = String.format(selectSqlByMd5, tableName);

  public static final String selectSqlById = "select md5,name,size,bucket_name,target_name from %s where id=? and deleted=0";
  public static final String getFileBasicInfoByIdSql = String.format(selectSqlById, tableName);

  public static final String selectSqlByIds = "select id,md5,name,size,platform,region_name,bucket_name,target_name from %s where id in (%s) and deleted=0";

  public Row getFileBasicInfoByMd5(String md5) {
    return Db.findFirst(getFileBasicInfoByMd5Sql, md5);
  }

  public Row getFileBasicInfoById(long id) {
    return Db.findFirst(getFileBasicInfoByIdSql, id);
  }

  public List<Row> getFileBasicInfoByIds(List<Long> file_ids) {

    if (file_ids == null || file_ids.isEmpty()) {
      return new ArrayList<>();
    }

    // Create a comma-separated string of placeholders "?, ?, ...".
    String placeholders = file_ids.stream().map(id -> "?").collect(Collectors.joining(","));

    // Build the SQL statement using the placeholders.
    String sql = String.format(selectSqlByIds, tableName, placeholders);

    // Execute the query and return the results.
    return Db.find(sql, file_ids.toArray());
  }

  public boolean save(long id, String md5, String originname, int fileSize, String platform, String bucketName,
      //
      String targetName) {
    Row record = Row.by("id", id)
        //
        .set("md5", md5).set("name", originname).set("size", fileSize)
        //
        .set("platform", platform).set("bucket_name", bucketName)
        //
        .set("target_name", targetName);

    return Db.save(tableName, record);
  }

}
