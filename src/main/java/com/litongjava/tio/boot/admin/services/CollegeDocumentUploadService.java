package com.litongjava.tio.boot.admin.services;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Record;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.table.model.TableInput;
import com.litongjava.table.model.TableResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollegeDocumentUploadService {
  
  public void afterSaveOrUpdate(String f, TableInput kv, TableResult<Kv> dbJsonBean) {
    Kv outputKv = Aop.get(DocumentService.class).generateAndSavePDFthumbnail("sjsu/documents", kv);
    List<Kv> images = new ArrayList<>();
    images.add(outputKv);

    Record record = new Record();
    record.set("files", images);
    Long longId = dbJsonBean.getData().getLong("id");
    record.set("id", longId);

    String[] jsonFields = { "files" };
    boolean update = Db.update(f, "id", record, jsonFields);
    log.info("update result:{},{},{}", f, longId, update);
  }

}
