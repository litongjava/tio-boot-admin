package com.litongjava.tio.boot.admin.services.storage;

import com.aliyun.oss.OSS;
import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.litongjava.tio.boot.admin.consts.StoragePlatformConst;
import com.litongjava.tio.boot.admin.consts.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.dao.SystemUploadFileDao;
import com.litongjava.tio.boot.admin.services.StorageService;
import com.litongjava.tio.boot.admin.services.system.SystemUploadFileService;
import com.litongjava.tio.boot.admin.utils.storage.AliyunOssUtils;

import lombok.extern.slf4j.Slf4j;
import nexus.io.db.TableInput;
import nexus.io.db.TableResult;
import nexus.io.db.activerecord.Row;
import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.model.upload.UploadFile;
import nexus.io.model.upload.UploadResult;
import nexus.io.table.services.ApiTable;
import nexus.io.tio.utils.crypto.Md5Utils;
import nexus.io.tio.utils.hutool.FilenameUtils;
import nexus.io.tio.utils.snowflake.SnowflakeIdUtils;

@Slf4j
public class AliyunStorageService implements StorageService {

  @Override
  public RespBodyVo upload(UploadFile uploadFile) {
    return upload(DEFAULT_CATEGORY, uploadFile);
  }

  @Override
  public RespBodyVo upload(String category, UploadFile uploadFile) {
    if (StrKit.isBlank(category)) {
      category = DEFAULT_CATEGORY;
    }
    UploadResult uploadResultVo = uploadFile(category, uploadFile);
    return RespBodyVo.ok(uploadResultVo);
  }

  @Override
  public UploadResult uploadFile(String category, UploadFile uploadFile) {
    long id = SnowflakeIdUtils.id();
    return uploadFile(category, uploadFile, id);
  }

  @Override
  public UploadResult uploadFile(String category, UploadFile uploadFile, Long id) {
    String suffix = FilenameUtils.getSuffix(uploadFile.getName());
    String newFilename = id + "." + suffix;
    String targetName = category + "/" + newFilename;
    return uploadFile(id, targetName, uploadFile, suffix);
  }

  @Override
  public UploadResult uploadFile(long id, String targetName, UploadFile uploadFile, String suffix) {
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

      return new UploadResult(id, name, size, url, md5);
    } else {
      log.info("not found from cache table: {}", md5);
    }

    String etag = null;

    OSS client = null;
    try {
      client = AliyunOssUtils.buildClient();
      etag = AliyunOssUtils.upload(client, AliyunOssUtils.bucketName, targetName, fileContent, suffix).getETag();
    } catch (Exception e) {
      log.error("Error uploading file", e);
      throw new RuntimeException(e);
    } finally {
      if (client != null) {
        client.shutdown();
      }
    }

    // 记录入库
    log.info("Uploaded to Aliyun OSS with ETag: {}", etag);

    TableInput kv = TableInput.create().set("name", name).set("size", size).set("md5", md5)
        .set("platform", StoragePlatformConst.aliyun_oss).set("region_name", AliyunOssUtils.regionName)
        .set("bucket_name", AliyunOssUtils.bucketName).set("target_name", targetName).set("file_id", etag);

    TableResult<Kv> save = ApiTable.save(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);
    String downloadUrl = getUrl(AliyunOssUtils.bucketName, targetName);

    return new UploadResult(save.getData().getLong("id"), name, size, downloadUrl, md5);
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
  public UploadResult getUrlById(String id) {
    return Aop.get(SystemUploadFileService.class).getUrlById(id);
  }

  @Override
  public UploadResult getUrlById(long id) {
    return Aop.get(SystemUploadFileService.class).getUrlById(id);
  }

  @Override
  public UploadResult getUrlByMd5(String md5) {
    return Aop.get(SystemUploadFileService.class).getUrlByMd5(md5);
  }

  @Override
  public String getPresignedDownloadUrl(String targetName) {
    return AliyunOssUtils.getPresignedDownloadUrl(targetName);
  }

  @Override
  public String getPresignedDownloadUrl(String bucket, String targetName) {
    return AliyunOssUtils.getPresignedDownloadUrl(bucket, targetName);
  }

  @Override
  public String getPresignedDownloadUrl(String region, String bucket, String targetName) {
    return AliyunOssUtils.getPresignedDownloadUrl(region, bucket, targetName);
  }

  @Override
  public UploadResult getPresignedDownloadUrl(Long id) {
    return Aop.get(SystemUploadFileService.class).getPresignedDownloadUrl(id);
  }

}
