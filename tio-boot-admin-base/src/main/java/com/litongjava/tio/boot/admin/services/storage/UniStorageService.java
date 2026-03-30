package com.litongjava.tio.boot.admin.services.storage;

import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.upload.UploadFile;
import com.litongjava.model.upload.UploadResult;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.admin.consts.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.dao.SystemUploadFileDao;
import com.litongjava.tio.boot.admin.services.StorageService;
import com.litongjava.tio.boot.admin.services.system.SystemUploadFileService;
import com.litongjava.tio.boot.admin.utils.storage.UniStorageUploadUtils;
import com.litongjava.tio.utils.crypto.Md5Utils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UniStorageService implements StorageService {
  @Override
  public RespBodyVo upload(UploadFile uploadFile) {
    return upload(DEFAULT_CATEGORY, uploadFile);
  }

  public RespBodyVo upload(String category, UploadFile uploadFile) {
    if (StrKit.isBlank(category)) {
      category = DEFAULT_CATEGORY;
    }
    UploadResult uploadResultVo = uploadFile(category, uploadFile);

    return RespBodyVo.ok(uploadResultVo);
  }

  public UploadResult uploadFile(String category, UploadFile uploadFile) {
    long id = SnowflakeIdUtils.id();
    return uploadFile(category, uploadFile, id);
  }

  public UploadResult uploadFile(String category, UploadFile uploadFile, Long id) {
    // 上传文件
    String name = uploadFile.getName();
    String suffix = FilenameUtils.getSuffix(name);
    String newFilename = id + "." + suffix;
    String targetName = category + "/" + newFilename;
    return uploadFile(id, targetName, uploadFile, suffix);
  }

  /**
   * @param id
   * @param originFilename
   * @param targetName
   * @param fileContent
   * @param size
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
      log.info("select table reuslt:{}", record.toMap());
      id = record.getLong("id");
      String url = this.getUrl(record.getStr("bucket_name"), record.getStr("target_name"));
      Kv kv = record.toKv();
      kv.remove("target_name");
      kv.remove("bucket_name");
      kv.set("url", url);
      kv.set("md5", md5);
      return new UploadResult(id, name, size, url, md5);
    } else {
      log.info("not found from cache table:{}", md5);
    }

    String etag = UniStorageUploadUtils.upload(targetName, fileContent, suffix);
    // Log and save to database
    log.info("Uploaded with ETag: {}", etag);

    String regionName = UniStorageUploadUtils.getRegionName();
    String bucketName = UniStorageUploadUtils.getBucketName();
    TableInput kv = TableInput.create().set("name", name).set("size", size).set("md5", md5)
        //
        .set("platform", UniStorageUploadUtils.storagePlatform).set("region_name", regionName).set("bucket_name", bucketName)
        //
        .set("target_name", targetName).set("file_id", etag);

    TableResult<Kv> save = ApiTable.save(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);
    String downloadUrl = getUrl(targetName);

    return new UploadResult(save.getData().getLong("id"), name, Long.valueOf(size), downloadUrl, md5);

  }

  @Override
  public String getUrl(String bucketName, String targetName) {
    return UniStorageUploadUtils.getUrl(bucketName, targetName);
  }

  @Override
  public String getUrl(String targetName) {
    return UniStorageUploadUtils.getUrl(targetName);
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
    return UniStorageUploadUtils.getPresignedDownloadUrl(targetName);
  }

  @Override
  public String getPresignedDownloadUrl(String bucket, String targetName) {
    return UniStorageUploadUtils.getPresignedDownloadUrl(bucket, targetName);
  }

  @Override
  public String getPresignedDownloadUrl(String region, String bucket, String targetName) {
    return UniStorageUploadUtils.getPresignedDownloadUrl(region, bucket, targetName);
  }

  @Override
  public UploadResult getPresignedDownloadUrl(Long id) {
    return Aop.get(SystemUploadFileService.class).getPresignedDownloadUrl(id);
  }

}
