package com.litongjava.tio.boot.admin.services.storage;

import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.litongjava.tio.boot.admin.consts.StoragePlatformConst;
import com.litongjava.tio.boot.admin.consts.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.dao.SystemUploadFileDao;
import com.litongjava.tio.boot.admin.services.StorageService;
import com.litongjava.tio.boot.admin.services.system.SystemUploadFileService;
import com.litongjava.tio.boot.admin.utils.storage.GoogleStorageUtils;

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
public class GoogleStorageService implements StorageService {

  @Override
  public RespBodyVo upload(UploadFile uploadFile) {
    return upload(DEFAULT_CATEGORY, uploadFile);
  }

  public RespBodyVo upload(String category, UploadFile uploadFile) {
    if (StrKit.isBlank(category)) {
      category = DEFAULT_CATEGORY;
    }
    UploadResult uploadResult = uploadFile(category, uploadFile);
    return RespBodyVo.ok(uploadResult);
  }

  public UploadResult uploadFile(String category, UploadFile uploadFile) {
    long id = SnowflakeIdUtils.id();
    return uploadFile(category, uploadFile, id);
  }

  public UploadResult uploadFile(String category, UploadFile uploadFile, Long id) {
    String name = uploadFile.getName();
    String suffix = FilenameUtils.getSuffix(name);
    String newFilename = id + "." + suffix;
    String targetName = category + "/" + newFilename;
    return uploadFile(id, targetName, uploadFile, suffix);
  }

  /**
   * @param id
   * @param targetName
   * @param uploadFile
   * @param suffix
   * @return
   */
  public UploadResult uploadFile(long id, String targetName, UploadFile uploadFile, String suffix) {
    String name = uploadFile.getName();
    long size = uploadFile.getSize();
    byte[] fileContent = uploadFile.getData();

    String md5 = Md5Utils.md5Hex(fileContent);
    Row record = Aop.get(SystemUploadFileDao.class).getFileBasicInfoByMd5(md5);

    if (record != null) {
      log.info("select table result: {}", record.toMap());
      id = record.getLong("id");

      String str = record.getStr("target_name");
      String url = this.getUrl(record.getStr("bucket_name"), str);

      Kv kv = record.toKv();
      kv.remove("target_name");
      kv.remove("bucket_name");
      kv.set("url", url);
      kv.set("md5", md5);

      UploadResult uploadResult = new UploadResult(id, name, size, url, md5);
      uploadResult.setTargetName(targetName);
      return uploadResult;
    } else {
      log.info("not found from cache table: {}", md5);
    }

    String fileId = null;
    try {
      fileId = GoogleStorageUtils.upload(GoogleStorageUtils.bucketName, targetName, fileContent, suffix);
    } catch (Exception e) {
      log.error("Error uploading file to Google Cloud Storage", e);
      throw new RuntimeException(e);
    }

    log.info("Uploaded to Google Cloud Storage, object: {}", targetName);

    TableInput kv = TableInput.create().set("name", name).set("size", size).set("md5", md5)
        .set("platform", StoragePlatformConst.google).set("region_name", GoogleStorageUtils.regionName)
        .set("bucket_name", GoogleStorageUtils.bucketName).set("target_name", targetName).set("file_id", fileId);

    TableResult<Kv> save = ApiTable.save(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);

    String downloadUrl = getUrl(GoogleStorageUtils.bucketName, targetName);

    UploadResult uploadResult = new UploadResult(save.getData().getLong("id"), name, size, downloadUrl, md5);
    uploadResult.setTargetName(targetName);
    return uploadResult;
  }

  @Override
  public String getUrl(String bucketName, String targetName) {
    return GoogleStorageUtils.getUrl(bucketName, targetName);
  }

  @Override
  public String getUrl(String targetName) {
    return GoogleStorageUtils.getUrl(targetName);
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
    return GoogleStorageUtils.getPresignedDownloadUrl(targetName);
  }

  @Override
  public String getPresignedDownloadUrl(String bucket, String targetName) {
    return GoogleStorageUtils.getPresignedDownloadUrl(bucket, targetName);
  }

  @Override
  public String getPresignedDownloadUrl(String region, String bucket, String targetName) {
    return GoogleStorageUtils.getPresignedDownloadUrl(region, bucket, targetName);
  }

  @Override
  public UploadResult getPresignedDownloadUrl(Long id) {
    return Aop.get(SystemUploadFileService.class).getPresignedDownloadUrl(id);
  }
}