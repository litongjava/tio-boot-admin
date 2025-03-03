package com.litongjava.tio.boot.admin.dao;

import java.util.List;

import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.satoken.SaJdkSerializer;

public class AdminTokenDbDao {

  public static final String tableName = "tio_boot_admin_admin_token";
  String get_value_sql = String.format("select value from %s where id=?", tableName);
  String get_timeout_sql = String.format("select timeout from %s where id=?", tableName);
  String get_object_sql = String.format("select ob from %s where id=?", tableName);
  String get_object_timeout_sql = String.format("select ob_timeout from %s where id=?", tableName);

  public String get(String key) {
    return Db.queryStr(get_value_sql, key);
  }

  public void set(String key, String value, long timeout) {
    Row record = Row.by("id", key).set("value", value).set("timeout", timeout);
    if (Db.exists(tableName, "id", key)) {
      Db.update(tableName, record);
    } else {
      Db.save(tableName, record);
    }
  }

  public void update(String key, String value) {
    Row record = Row.by("id", key).set("value", value);
    Db.update(tableName, record);
  }

  public void delete(String key) {
    Db.deleteById(tableName, "id", key);
  }

  public long getTimeout(String key) {
    return Db.queryLong(get_timeout_sql, key);
  }

  public void updateTimeout(String key, long timeout) {
    Row record = Row.by("id", key).set("timeout", timeout);
    Db.update(tableName, "id", record);
  }

  public Object getObject(String key) {
    byte[] queryFirst = Db.quereyBytes(get_object_sql, key);
    return SaJdkSerializer.me.valueFromBytes(queryFirst);

  }

  public void setObject(String key, Object object, long timeout) {
    byte[] valueToBytes = SaJdkSerializer.me.valueToBytes(object);
    Row record = Row.by("id", key).set("ob", valueToBytes).set("ob_timeout", timeout);
    if (Db.exists(tableName, "id", key)) {
      Db.update(tableName, record);
    } else {
      Db.save(tableName, record);
    }

  }

  public void updateObject(String key, Object object) {
    byte[] valueToBytes = SaJdkSerializer.me.valueToBytes(object);
    Row record = Row.by("id", key).set("ob", valueToBytes);
    Db.update(tableName, "id", record);
  }

  public void deleteObject(String key) {
    Db.deleteById(tableName, "id", key);
  }

  public long getObjectTimeout(String key) {
    return Db.queryLong(get_object_timeout_sql, key);
  }

  public void updateObjectTimeout(String key, long timeout) {
    Row record = Row.by("id", key).set("ob_timeout", timeout);
    Db.update(tableName, "id", record);
  }

  public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
    return null;
  }

}
