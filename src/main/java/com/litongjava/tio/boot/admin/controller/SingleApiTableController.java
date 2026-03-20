package com.litongjava.tio.boot.admin.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.Kv;
import com.litongjava.annotation.RequestPath;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.page.DbPage;
import com.litongjava.model.page.Page;
import com.litongjava.table.services.ApiTable;
import com.litongjava.table.utils.EasyExcelResponseUtils;
import com.litongjava.table.utils.TableInputUtils;
import com.litongjava.table.utils.TableResultUtils;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.boot.utils.TioRequestParamUtils;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;

public abstract class SingleApiTableController {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final String from;

  protected SingleApiTableController(String from) {
    this.from = from;
  }

  @RequestPath("/create")
  public RespBodyVo create(HttpRequest request) {

    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(from, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);
    log.info("tableName:{},kv:{}", from, kv);
    TableResult<Kv> dbJsonBean = ApiTable.saveOrUpdate(from, kv);
    if (dbJsonBean.getCode() == 1) {
      return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
    } else {
      return RespBodyVo.fail(dbJsonBean.getMsg()).code(dbJsonBean.getCode()).data(dbJsonBean.getData());
    }

  }

  @RequestPath("/list")
  public RespBodyVo list(HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(from, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", from, kv);
    TableResult<List<Row>> list = ApiTable.list(from, kv);

    TableResult<List<Kv>> dbJsonBean = TableResultUtils.recordsToKv(list, false);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/listAll")
  public RespBodyVo listAll(String f) {
    log.info("tableName:{}", f);
    TableResult<List<Row>> listAll = ApiTable.listAll(f);
    TableResult<List<Kv>> dbJsonBean = TableResultUtils.recordsToKv(listAll, false);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/page")
  public RespBodyVo page(HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Object current = map.remove("current");
    if (current != null) {
      // add support for ant design pro table
      map.put("pageNo", current);
    }

    ApiTable.transformType(from, map);

    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", from, kv);
    TableResult<Page<Row>> page = ApiTable.page(from, kv);

    TableResult<DbPage<Kv>> dbJsonBean = TableResultUtils.pageToDbPage(page, false);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/get")
  public RespBodyVo get(HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(from, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", from, kv);
    TableResult<Row> jsonBean = ApiTable.get(from, kv);
    TableResult<Kv> dbJsonBean = TableResultUtils.recordToKv(jsonBean);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/update")
  public RespBodyVo update(HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(from, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", from, kv);
    TableResult<Kv> dbJsonBean = ApiTable.saveOrUpdate(from, kv);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/batchUpdate")
  public RespBodyVo batchUpdate(HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(from, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", from, kv);
    TableResult<Kv> dbJsonBean = ApiTable.batchUpdateByIds(from, kv);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/remove/{id}")
  public RespBodyVo remove(String id) {
    log.info("tableName:{},id:{}", from, id);
    TableResult<Boolean> dbJsonBean = ApiTable.updateFlagById(from, id, "deleted", 1);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/delete/{id}")
  public RespBodyVo delete(String id) {
    log.info("tableName:{},id:{}", from, id);
    TableResult<Boolean> dbJsonBean = ApiTable.delById(from, id);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/total")
  public RespBodyVo total(String f) {
    log.info("tableName:{},id:{}", f);
    Long count = Db.count(f);
    return RespBodyVo.ok(count);
  }

  /**
   * 导出当前数据
   */
  @RequestPath("/export-current")
  public HttpResponse exportCurrent(HttpRequest request) throws IOException {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(from, map);
    Object current = map.remove("current");
    if (current != null) {
      // add support for ant design pro table
      map.put("pageNo", current);
    }
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", from, kv);
    String filename = from + "_export-current-page" + System.currentTimeMillis() + ".xlsx";

    // 获取数据
    TableResult<Page<Row>> result = ApiTable.page(from, kv);
    List<Row> records = result.getData().getList();

    HttpResponse response = TioRequestContext.getResponse();
    EasyExcelResponseUtils.exportRecords(response, filename, from, records);
    log.info("finished {}", filename);
    return response;
  }

  /**
   * 导出所有数据
   */
  @RequestPath("/export-all")
  public HttpResponse exportAllExcel(HttpRequest request) throws IOException, SQLException {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    map.remove("current");
    map.remove("pageNo");
    map.remove("pageSize");
    ApiTable.transformType(from, map);

    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", from, kv);

    // 导出 Excel
    String filename = from + "-all_" + System.currentTimeMillis() + ".xlsx";

    // 获取数据
    TableResult<List<Row>> result = ApiTable.list(from, kv);
    List<Row> records = result.getData();
    HttpResponse response = TioRequestContext.getResponse();
    EasyExcelResponseUtils.exportRecords(response, filename, from, records);
    log.info("finished {}", filename);
    return response;
  }

  @RequestPath("/pageDeleted")
  public RespBodyVo pageDeleted(HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    ApiTable.transformType(from, map);
    TableInput kv = TableInputUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", from, kv);
    TableResult<DbPage<Kv>> dbJsonBean = TableResultUtils.pageToDbPage(ApiTable.page(from, kv), false);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/recover")
  public RespBodyVo recover(String id) {
    log.info("tableName:{},id:{}", from, id);
    TableResult<Boolean> dbJsonBean = ApiTable.updateFlagById(from, id, "deleted", 0);

    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/names")
  public RespBodyVo tableNames() throws IOException {
    String[] data = ApiTable.tableNames().getData();
    return RespBodyVo.ok(data);
  }

  @RequestPath("/config")
  public RespBodyVo fConfig(String lang) {
    log.info("tableName:{}", from);
    TableResult<Map<String, Object>> dbJsonBean = ApiTable.tableConfig(from, from, lang);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  @RequestPath("/columns")
  public RespBodyVo proTableColumns(String f) {
    TableResult<List<Map<String, Object>>> dbJsonBean = ApiTable.columns(f);
    return RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }
}
