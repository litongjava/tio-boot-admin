package com.litongjava.tio.boot.admin.services;

import com.jfinal.kit.Kv;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.jfinal.aop.Aop;

//@Slf4j
public class TableJsonService {

  public void afterSaveOrUpdate(String f, TableInput kv, TableResult<Kv> dbJsonBean) {
    if ("rumi_sjsu_documents".equals(f)) {
      Aop.get(CollegeDocumentUploadService.class).afterSaveOrUpdate(f, kv, dbJsonBean);
      
    } else if ("rumi_uc_berkeley_documents".equals(f)) {
      Aop.get(CollegeDocumentUploadService.class).afterSaveOrUpdate(f, kv, dbJsonBean);
      
    }else if ("rumi_stanford_documents".equals(f)) {
      Aop.get(CollegeDocumentUploadService.class).afterSaveOrUpdate(f, kv, dbJsonBean);
      
    }else if ("rumi_uva_documents".equals(f)) {
      Aop.get(CollegeDocumentUploadService.class).afterSaveOrUpdate(f, kv, dbJsonBean);
      
    }else if ("rumi_harvard_documents".equals(f)) {
      Aop.get(CollegeDocumentUploadService.class).afterSaveOrUpdate(f, kv, dbJsonBean);
    }
  }

}
