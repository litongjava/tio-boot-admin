package com.litongjava.tio.boot.admin.services;

import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.vo.UploadResultVo;
import com.litongjava.tio.http.common.UploadFile;

public interface StorageService {

  public RespBodyVo upload(String category, UploadFile uploadFile);

  public UploadResultVo uploadBytes(String category, String originFilename, int size, byte[] fileContent);

  public UploadResultVo uploadBytes(long id, String originFilename, String targetName, byte[] fileContent,
      //
      int size, String suffix);

  public String getUrl(String bucketName, String targetName);

  public UploadResultVo getUrlById(String id);

  public UploadResultVo getUrlById(long id);

  public UploadResultVo getUrlByMd5(String md5);

}
