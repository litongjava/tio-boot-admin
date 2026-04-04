package com.litongjava.tio.boot.admin.utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.Kv;

import nexus.io.db.TableInput;
import nexus.io.db.TableResult;
import nexus.io.db.activerecord.Db;
import nexus.io.db.activerecord.Row;
import nexus.io.model.body.RespBodyVo;
import nexus.io.model.page.DbPage;
import nexus.io.model.page.Page;
import nexus.io.table.services.ApiTable;
import nexus.io.table.utils.EasyExcelResponseUtils;
import nexus.io.table.utils.TableInputUtils;
import nexus.io.table.utils.TableResultUtils;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.boot.utils.TioRequestParamUtils;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;

public class ApiTableUtils {

  private final static Logger log = LoggerFactory.getLogger(ApiTableUtils.class);

  public static RespBodyVo create(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(f, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);
    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Kv> dbJsonBean = ApiTable.saveOrUpdate(f, kv);
    if (dbJsonBean.getCode() == 1) {
      return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
    } else {
      return RespBodyVo.fail(dbJsonBean.getMsg()).code(dbJsonBean.getCode()).data(dbJsonBean.getData());
    }

  }

  public static RespBodyVo list(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(f, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<List<Row>> list = ApiTable.list(f, kv);

    TableResult<List<Kv>> dbJsonBean = TableResultUtils.recordsToKv(list, false);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo listAll(String f) {
    log.info("tableName:{}", f);
    TableResult<List<Row>> listAll = ApiTable.listAll(f);
    TableResult<List<Kv>> dbJsonBean = TableResultUtils.recordsToKv(listAll, false);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo page(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Object current = map.remove("current");
    if (current != null) {
      // add support for ant design pro table
      map.put("pageNo", current);
    }

    ApiTable.transformType(f, map);

    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Page<Row>> page = ApiTable.page(f, kv);

    TableResult<DbPage<Kv>> dbJsonBean = TableResultUtils.pageToDbPage(page, false);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo get(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(f, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Row> jsonBean = ApiTable.get(f, kv);
    TableResult<Kv> dbJsonBean = TableResultUtils.recordToKv(jsonBean);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo update(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(f, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Kv> dbJsonBean = ApiTable.saveOrUpdate(f, kv);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo batchUpdate(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(f, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Kv> dbJsonBean = ApiTable.batchUpdateByIds(f, kv);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo remove(String f, String id) {
    log.info("tableName:{},id:{}", f, id);
    TableResult<Boolean> dbJsonBean = ApiTable.updateFlagById(f, id, "deleted", 1);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo delete(String f, String id) {
    log.info("tableName:{},id:{}", f, id);
    TableResult<Boolean> dbJsonBean = ApiTable.delById(f, id);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo total(String f) {
    log.info("tableName:{},id:{}", f);
    Long count = Db.count(f);
    return RespBodyVo.ok(count);
  }

  /**
   * 导出当前数据
   */
  public static HttpResponse exportCurrent(String f, HttpRequest request) throws IOException {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(f, map);
    Object current = map.remove("current");
    if (current != null) {
      // add support for ant design pro table
      map.put("pageNo", current);
    }
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    String filename = f + "_export-current-page" + System.currentTimeMillis() + ".xlsx";

    // 获取数据
    TableResult<Page<Row>> result = ApiTable.page(f, kv);
    List<Row> records = result.getData().getList();

    HttpResponse response = TioRequestContext.getResponse();
    EasyExcelResponseUtils.exportRecords(response, filename, f, records);
    log.info("finished {}", filename);
    return response;
  }

  /**
   * 导出所有数据
   */
  public static HttpResponse exportAllExcel(String f, HttpRequest request) throws IOException, SQLException {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    map.remove("current");
    map.remove("pageNo");
    map.remove("pageSize");
    ApiTable.transformType(f, map);

    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);

    // 导出 Excel
    String filename = f + "-all_" + System.currentTimeMillis() + ".xlsx";

    // 获取数据
    TableResult<List<Row>> result = ApiTable.list(f, kv);
    List<Row> records = result.getData();
    HttpResponse response = TioRequestContext.getResponse();
    EasyExcelResponseUtils.exportRecords(response, filename, f, records);
    log.info("finished {}", filename);
    return response;
  }

  public HttpResponse exportAllTableExcel(HttpRequest request) throws IOException {
    String filename = "all-table_" + System.currentTimeMillis() + ".xlsx";
    String[] tables = ApiTable.getAllTableNames();
    LinkedHashMap<String, List<Row>> allTableData = new LinkedHashMap<>();

    for (String table : tables) {
      // 获取数据
      List<Row> records = ApiTable.listAll(table).getData();
      allTableData.put(table, records);
    }
    HttpResponse response = TioRequestContext.getResponse();
    EasyExcelResponseUtils.exportAllTableRecords(response, filename, allTableData);
    log.info("finished");
    return response;
  }

  public static RespBodyVo pageDeleted(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(f, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<DbPage<Kv>> dbJsonBean = TableResultUtils.pageToDbPage(ApiTable.page(f, kv), false);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo recover(String f, String id) {
    log.info("tableName:{},id:{}", f, id);
    TableResult<Boolean> dbJsonBean = ApiTable.updateFlagById(f, id, "deleted", 0);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo tableNames() throws IOException {
    String[] data = ApiTable.tableNames().getData();
    return RespBodyVo.ok(data);
  }

  public static RespBodyVo fConfig(String f, String lang) {
    log.info("tableName:{}", f);
    TableResult<Map<String, Object>> dbJsonBean = ApiTable.tableConfig(f, f, lang);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public static RespBodyVo proTableColumns(String f) {
    TableResult<List<Map<String, Object>>> dbJsonBean = ApiTable.columns(f);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }
}
