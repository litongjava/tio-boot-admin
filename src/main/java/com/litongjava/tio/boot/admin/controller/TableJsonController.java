package com.litongjava.tio.boot.admin.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.model.DbPage;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.data.utils.DbJsonBeanUtils;
import com.litongjava.data.utils.EasyExcelResponseUtils;
import com.litongjava.data.utils.KvUtils;
import com.litongjava.jfinal.aop.annotation.AAutowired;
import com.litongjava.jfinal.plugin.activerecord.Page;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.services.TableJsonService;
import com.litongjava.tio.boot.http.TioHttpContext;
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
  private DbJsonService dbJsonService;

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
    Kv kv = KvUtils.camelToUnderscore(map);
    log.info("tableName:{},kv:{}", f, kv);
    DbJsonBean<Kv> dbJsonBean = dbJsonService.saveOrUpdate(f, kv);
    if (dbJsonBean.getCode() == 1) {
      tableJsonService.afterSaveOrUpdate(f, kv, dbJsonBean);
      return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
    } else {
      return RespVo.fail(dbJsonBean.getMsg()).code(dbJsonBean.getCode()).data(dbJsonBean.getData());
    }

  }

  @RequestPath("/{f}/list")
  public RespVo list(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Kv kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    DbJsonBean<List<Record>> list = dbJsonService.list(f, kv);
    DbJsonBean<List<Kv>> dbJsonBean = DbJsonBeanUtils.recordsToKv(list, false);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/listAll")
  public RespVo listAll(String f) {
    log.info("tableName:{}", f);
    DbJsonBean<List<Record>> listAll = dbJsonService.listAll(f);
    DbJsonBean<List<Kv>> dbJsonBean = DbJsonBeanUtils.recordsToKv(listAll, false);

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
    Kv kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    DbJsonBean<Page<Record>> page = dbJsonService.page(f, kv);

    DbJsonBean<DbPage<Kv>> dbJsonBean = DbJsonBeanUtils.pageToDbPage(page, false);
    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/get")
  public RespVo get(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Kv kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    DbJsonBean<Record> jsonBean = dbJsonService.get(f, kv);
    DbJsonBean<Kv> dbJsonBean = DbJsonBeanUtils.recordToKv(jsonBean);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/update")
  public RespVo update(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Kv kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    DbJsonBean<Kv> dbJsonBean = dbJsonService.saveOrUpdate(f, kv);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/batchUpdate")
  public RespVo batchUpdate(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Kv kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    DbJsonBean<Kv> dbJsonBean = dbJsonService.batchUpdateByIds(f, kv);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/delete/{id}")
  public RespVo delete(String f, String id) {
    log.info("tableName:{},id:{}", f, id);
    DbJsonBean<Boolean> dbJsonBean = dbJsonService.updateFlagById(f, id, "deleted", 1);
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
    Kv kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    String filename = f + "_export_" + System.currentTimeMillis() + ".xlsx";

    // 获取数据
    List<Record> records = dbJsonService.list(f, kv).getData();
    return EasyExcelResponseUtils.exportRecords(request, filename, f, records);
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

    Kv kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);

    // 导出 Excel
    String filename = f + "-all_" + System.currentTimeMillis() + ".xlsx";

    // 获取数据
    List<Record> records = dbJsonService.listAll(f, kv).getData();

    HttpResponse response = EasyExcelResponseUtils.exportRecords(request, filename, f, records);
    log.info("finished");
    return response;
  }

  @RequestPath("/export-all-table-excel")
  public HttpResponse exportAllTableExcel(HttpRequest request) throws IOException {
    String filename = "all-table_" + System.currentTimeMillis() + ".xlsx";
    String[] tables = dbJsonService.getAllTableNames();
    LinkedHashMap<String, List<Record>> allTableData = new LinkedHashMap<>();

    for (String table : tables) {
      // 获取数据
      List<Record> records = dbJsonService.listAll(table).getData();
      allTableData.put(table, records);
    }
    HttpResponse response = TioHttpContext.getResponse();
    EasyExcelResponseUtils.exportAllTableRecords(response, filename, allTableData);
    log.info("finished");
    return response;
  }

  @RequestPath("/{f}/pageDeleted")
  public RespVo pageDeleted(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Kv kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    DbJsonBean<DbPage<Kv>> dbJsonBean = DbJsonBeanUtils.pageToDbPage(dbJsonService.page(f, kv), false);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/recover")
  public RespVo recover(String f, String id) {
    log.info("tableName:{},id:{}", f, id);
    DbJsonBean<Boolean> dbJsonBean = dbJsonService.updateFlagById(f, id, "deleted", 0);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/names")
  public RespVo tableNames() throws IOException {
    String[] data = dbJsonService.tableNames().getData();
    return RespVo.ok(data);
  }

  @RequestPath("/{f}/config")
  public RespVo fConfig(String f, String lang) {
    log.info("tableName:{}", f);
    DbJsonBean<Map<String, Object>> dbJsonBean = dbJsonService.tableConfig(f, f, lang);
    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/{f}/columns")
  public RespVo proTableColumns(String f) {
    DbJsonBean<List<Map<String, Object>>> dbJsonBean = dbJsonService.columns(f);
    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }
}
