package com.litongjava.tio.boot.admin.services.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.admin.consts.StoragePlatformConst;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.dao.SystemUploadFileDao;
import com.litongjava.tio.boot.admin.services.StorageService;
import com.litongjava.tio.boot.admin.services.system.SystemUploadFileService;
import com.litongjava.tio.boot.admin.utils.AliyunOssUtils;
import com.litongjava.tio.boot.admin.vo.UploadResultVo;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.utils.crypto.Md5Utils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AliyunStorageService implements StorageService {

  @Override
  public RespBodyVo upload(String category, UploadFile uploadFile) {
    if (StrKit.isBlank(category)) {
      category = "default";
    }
    UploadResultVo uploadResultVo = uploadFile(category, uploadFile);
    return RespBodyVo.ok(uploadResultVo);
  }

  @Override
  public UploadResultVo uploadFile(String category, UploadFile uploadFile) {
    long id = SnowflakeIdUtils.id();
    String suffix = FilenameUtils.getSuffix(uploadFile.getName());
    String newFilename = id + "." + suffix;
    String targetName = category + "/" + newFilename;
    return uploadFile(id, targetName, uploadFile, suffix);
  }

  @Override
  public UploadResultVo uploadFile(long id, String targetName, UploadFile uploadFile, String suffix) {
    String name = uploadFile.getName();
    long size = uploadFile.getSize();
    byte[] fileContent = uploadFile.getData();

    // 计算 MD5 幂等去重
    String md5 = Md5Utils.md5Hex(fileContent);
    Row record = Aop.get(SystemUploadFileDao.class).getFileBasicInfoByMd5(md5);
    if (record != null) {
      log.info("select table result: {}", record.toMap());
      id = record.getLong("id");
      String url = this.getUrl(record.getStr("bucket_name"), record.getStr("target_name"));

      Kv kv = record.toKv();
      kv.remove("target_name");
      kv.remove("bucket_name");
      kv.set("url", url);
      kv.set("md5", md5);

      return new UploadResultVo(id, name, size, url, md5);
    } else {
      log.info("not found from cache table: {}", md5);
    }

    String etag = null;

    OSS client = null;
    try {
      client = AliyunOssUtils.buildClient();
      PutObjectResult result = AliyunOssUtils.upload(client, AliyunOssUtils.bucketName, targetName, fileContent, suffix);
      etag = result.getETag();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      if (client != null) {
        client.shutdown();
      }
    }

    // 记录入库
    log.info("Uploaded to Aliyun OSS with ETag: {}", etag);

    TableInput kv = TableInput.create().set("name", name).set("size", size).set("md5", md5).set("platform", StoragePlatformConst.aliyun_oss)
        .set("region_name", AliyunOssUtils.regionName).set("bucket_name", AliyunOssUtils.bucketName).set("target_name", targetName)
        .set("file_id", etag);

    TableResult<Kv> save = ApiTable.save(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);
    String downloadUrl = getUrl(AliyunOssUtils.bucketName, targetName);

    return new UploadResultVo(save.getData().getLong("id"), name, size, downloadUrl, md5);
  }

  @Override
  public String getUrl(String bucketName, String targetName) {
    return AliyunOssUtils.getUrl(bucketName, targetName);
  }

  @Override
  public String getUrl(String targetName) {
    return AliyunOssUtils.getUrl(targetName);
  }

  @Override
  public UploadResultVo getUrlById(String id) {
    return Aop.get(SystemUploadFileService.class).getUrlById(id);
  }

  @Override
  public UploadResultVo getUrlById(long id) {
    return Aop.get(SystemUploadFileService.class).getUrlById(id);
  }

  @Override
  public UploadResultVo getUrlByMd5(String md5) {
    return Aop.get(SystemUploadFileService.class).getUrlByMd5(md5);
  }

}
