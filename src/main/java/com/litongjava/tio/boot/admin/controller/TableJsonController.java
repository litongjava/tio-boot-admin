package com.litongjava.tio.boot.admin.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Page;
import com.litongjava.db.activerecord.Record;
import com.litongjava.jfinal.aop.annotation.AAutowired;
import com.litongjava.table.model.DbPage;
import com.litongjava.table.model.TableInput;
import com.litongjava.table.model.TableResult;
import com.litongjava.table.services.ApiTable;
import com.litongjava.table.utils.EasyExcelResponseUtils;
import com.litongjava.table.utils.KvUtils;
import com.litongjava.table.utils.TableResultUtils;
import com.litongjava.tio.boot.admin.services.TableJsonService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.boot.utils.TioRequestParamUtils;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.annotation.EnableCORS;
import com.litongjava.tio.http.server.annotation.RequestPath;
import com.litongjava.tio.utils.resp.RespVo;

import lombok.extern.slf4j.Slf4j;

@RequestPath("/table/json")
@Slf4j
@EnableCORS
public class TableJsonController {

  @AAutowired
  private TableJsonService tableJsonService;

  @RequestPath("/index")
  public String index() {
    return "TableJsonController";
  }

  @RequestPath("/{f}/create")
  public RespVo create(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    TableInput kv = KvUtils.camelToUnderscore(map);
    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Kv> dbJsonBean = ApiTable.saveOrUpdate(f, kv);
    if (dbJsonBean.getCode() == 1) {
      if(tableJsonService!=null) {
        tableJsonService.afterSaveOrUpdate(f, kv, dbJsonBean);
      }
      return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
    } else {
      return RespVo.fail(dbJsonBean.getMsg()).code(dbJsonBean.getCode()).data(dbJsonBean.getData());
    }

  }

  @RequestPath("/{f}/list")
  public RespVo list(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    TableInput kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<List<Record>> list = ApiTable.list(f, kv);
    
    TableResult<List<Kv>> dbJsonBean = TableResultUtils.recordsToKv(list, false);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/listAll")
  public RespVo listAll(String f) {
    log.info("tableName:{}", f);
    TableResult<List<Record>> listAll = ApiTable.listAll(f);
    TableResult<List<Kv>> dbJsonBean = TableResultUtils.recordsToKv(listAll, false);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/page")
  public RespVo page(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Object current = map.remove("current");
    if (current != null) {
      // add support for ant design pro table
      map.put("pageNo", current);
    }
    TableInput kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Page<Record>> page = ApiTable.page(f, kv);

    TableResult<DbPage<Kv>> dbJsonBean = TableResultUtils.pageToDbPage(page, false);
    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/get")
  public RespVo get(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    TableInput kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Record> jsonBean = ApiTable.get(f, kv);
    TableResult<Kv> dbJsonBean = TableResultUtils.recordToKv(jsonBean);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/update")
  public RespVo update(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    TableInput kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Kv> dbJsonBean = ApiTable.saveOrUpdate(f, kv);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/batchUpdate")
  public RespVo batchUpdate(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    TableInput kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Kv> dbJsonBean = ApiTable.batchUpdateByIds(f, kv);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/delete/{id}")
  public RespVo delete(String f, String id) {
    log.info("tableName:{},id:{}", f, id);
    TableResult<Boolean> dbJsonBean = ApiTable.updateFlagById(f, id, "deleted", 1);
    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  /**
   * 导出当前数据
   */
  @RequestPath("/{f}/export-excel")
  public HttpResponse exportExcel(String f, HttpRequest request) throws IOException {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Object current = map.remove("current");
    if (current != null) {
      // add support for ant design pro table
      map.put("pageNo", current);
    }
    TableInput kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    String filename = f + "_export_" + System.currentTimeMillis() + ".xlsx";

    // 获取数据
    List<Record> records = ApiTable.list(f, kv).getData();
    HttpResponse response = TioRequestContext.getResponse();
    return EasyExcelResponseUtils.exportRecords(response, filename, f, records);
  }

  /**
   * 导出所有数据
   */
  @RequestPath("/{f}/export-table-excel")
  public HttpResponse exportAllExcel(String f, HttpRequest request) throws IOException, SQLException {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    map.remove("current");
    map.remove("pageNo");
    map.remove("pageSize");

    TableInput kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);

    // 导出 Excel
    String filename = f + "-all_" + System.currentTimeMillis() + ".xlsx";

    // 获取数据
    List<Record> records = ApiTable.listAll(f, kv).getData();

    HttpResponse response = TioRequestContext.getResponse();
    EasyExcelResponseUtils.exportRecords(response, filename, f, records);
    log.info("finished");
    return response;
  }

  @RequestPath("/export-all-table-excel")
  public HttpResponse exportAllTableExcel(HttpRequest request) throws IOException {
    String filename = "all-table_" + System.currentTimeMillis() + ".xlsx";
    String[] tables = ApiTable.getAllTableNames();
    LinkedHashMap<String, List<Record>> allTableData = new LinkedHashMap<>();

    for (String table : tables) {
      // 获取数据
      List<Record> records = ApiTable.listAll(table).getData();
      allTableData.put(table, records);
    }
    HttpResponse response = TioRequestContext.getResponse();
    EasyExcelResponseUtils.exportAllTableRecords(response, filename, allTableData);
    log.info("finished");
    return response;
  }

  @RequestPath("/{f}/pageDeleted")
  public RespVo pageDeleted(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    TableInput kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    TableResult<DbPage<Kv>> dbJsonBean = TableResultUtils.pageToDbPage(ApiTable.page(f, kv), false);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/recover")
  public RespVo recover(String f, String id) {
    log.info("tableName:{},id:{}", f, id);
    TableResult<Boolean> dbJsonBean = ApiTable.updateFlagById(f, id, "deleted", 0);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/names")
  public RespVo tableNames() throws IOException {
    String[] data = ApiTable.tableNames().getData();
    return RespVo.ok(data);
  }

  @RequestPath("/{f}/config")
  public RespVo fConfig(String f, String lang) {
    log.info("tableName:{}", f);
    TableResult<Map<String, Object>> dbJsonBean = ApiTable.tableConfig(f, f, lang);
    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/columns")
  public RespVo proTableColumns(String f) {
    TableResult<List<Map<String, Object>>> dbJsonBean = ApiTable.columns(f);
    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }
}
