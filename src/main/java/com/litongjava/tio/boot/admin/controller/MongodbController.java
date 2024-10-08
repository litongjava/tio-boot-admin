package com.litongjava.tio.boot.admin.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Record;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.table.model.DbPage;
import com.litongjava.table.model.TableInput;
import com.litongjava.table.model.TableResult;
import com.litongjava.table.utils.EasyExcelResponseUtils;
import com.litongjava.table.utils.KvUtils;
import com.litongjava.tio.boot.admin.services.MongodbJsonService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.boot.utils.TioRequestParamUtils;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.annotation.EnableCORS;
import com.litongjava.tio.http.server.annotation.RequestPath;
import com.litongjava.tio.utils.resp.RespVo;

import lombok.extern.slf4j.Slf4j;

@RequestPath("/mongodb/json")
@Slf4j
@EnableCORS
public class MongodbController {

  private MongodbJsonService mongodbJsonService = Aop.get(MongodbJsonService.class);

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

    TableResult<DbPage<Document>> dbJsonBean = mongodbJsonService.page(f, kv);
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
    List<Record> records = mongodbJsonService.list(f, kv).getData();
    log.info("records:{}", records);
    HttpResponse response = TioRequestContext.getResponse();
    return EasyExcelResponseUtils.exportRecords(response, filename, f, records);
  }
  
  /**
   * 导出所有数据
   */
  @RequestPath("/{f}/export-table-excel")
  public HttpResponse exportAllExcel(String f, HttpRequest request) throws IOException, SQLException {
    log.info("tableName:{}", f);
    // 导出 Excel
    String filename = f + "-all_" + System.currentTimeMillis() + ".xlsx";

    // 获取数据
    List<Record> records = mongodbJsonService.listAll(f).getData();
    HttpResponse response = TioRequestContext.getResponse();
    EasyExcelResponseUtils.exportRecords(response, filename, f, records);
    log.info("finished");
    return response;
  }
  
  @RequestPath("/{f}/create")
  public RespVo create(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    TableInput kv = KvUtils.camelToUnderscore(map);
    log.info("tableName:{},kv:{}", f, kv);
    TableResult<Kv> dbJsonBean = mongodbJsonService.saveOrUpdate(f, kv);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }
  

  @RequestPath("/{f}/delete/{id}")
  public RespVo delete(String f, String id) {
    log.info("tableName:{},id:{}", f, id);
    TableResult<Boolean> dbJsonBean = mongodbJsonService.deleteById(f, id);
    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }


}
