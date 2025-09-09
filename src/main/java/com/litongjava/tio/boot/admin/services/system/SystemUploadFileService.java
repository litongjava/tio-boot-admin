package com.litongjava.tio.boot.admin.services.system;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.dao.SystemUploadFileDao;
import com.litongjava.tio.boot.admin.utils.AwsS3Utils;
import com.litongjava.tio.boot.admin.vo.UploadResultVo;

public class SystemUploadFileService {
  public UploadResultVo getUrlById(String id) {
    return getUrlById(Long.parseLong(id));
  }

  public UploadResultVo getUrlById(long id) {
    Row record = Aop.get(SystemUploadFileDao.class).getFileBasicInfoById(id);
    if (record == null) {
      return null;
    }
    String platform = record.getStr("platform");
    String region_name = record.getStr("region_name");
    String bucket_name = record.getStr("bucket_name");
    String target_name = record.getStr("target_name");
    
    String url = this.getUrl(platform, region_name, bucket_name, target_name);
    String originFilename = record.getStr("fielename");
    String md5 = record.getStr("md5");
    Long size = record.getLong("size");
    return new UploadResultVo(id, originFilename, size, url, md5);
  }

  public UploadResultVo getUrlByMd5(String md5) {
    Row record = Aop.get(SystemUploadFileDao.class).getFileBasicInfoByMd5(md5);
    if (record == null) {
      return null;
    }
    Long id = record.getLong("id");
    String platform = record.getStr("platform");
    String region_name = record.getStr("region_name");
    String bucket_name = record.getStr("bucket_name");
    String target_name = record.getStr("target_name");
    String url = this.getUrl(platform, region_name, bucket_name, target_name);
    Kv kv = record.toKv();
    kv.set("url", url);
    kv.set("md5", md5);
    String originFilename = record.getStr("filename");
    Long size = record.getLong("size");
    return new UploadResultVo(id, originFilename, size, url, md5);
  }

  public String getUrl(String platform, String region_name, String bucket_name, String targetName) {
    return String.format(AwsS3Utils.urlFormat, AwsS3Utils.bucketName, region_name, targetName);
  }

}
