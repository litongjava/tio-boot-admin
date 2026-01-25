package com.litongjava.tio.boot.admin.services;

import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.upload.UploadFile;
import com.litongjava.model.upload.UploadResult;

public interface StorageService {

  public RespBodyVo upload(String category, UploadFile uploadFile);

  public UploadResult uploadFile(String category, UploadFile uploadFile);

  public UploadResult uploadFile(long id, String targetName, UploadFile uploadFile, String suffix);

  public String getUrl(String bucketName, String targetName);
  
  public String getUrl(String targetName);

  public UploadResult getUrlById(String id);

  public UploadResult getUrlById(long id);

  public UploadResult getUrlByMd5(String md5);

}
