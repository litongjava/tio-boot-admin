package nexus.io.tio.boot.admin.services.storage;

import com.jfinal.kit.StrKit;
import com.qcloud.cos.COSClient;

import lombok.extern.slf4j.Slf4j;
import nexus.io.db.TableInput;
import nexus.io.db.activerecord.Row;
import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.model.upload.UploadFile;
import nexus.io.model.upload.UploadResult;
import nexus.io.table.services.ApiTable;
import nexus.io.tio.boot.admin.consts.StoragePlatformConst;
import nexus.io.tio.boot.admin.consts.TioBootAdminTableNames;
import nexus.io.tio.boot.admin.dao.SystemUploadFileDao;
import nexus.io.tio.boot.admin.services.StorageService;
import nexus.io.tio.boot.admin.services.SysConfigConstantsService;
import nexus.io.tio.boot.admin.services.system.SystemUploadFileService;
import nexus.io.tio.boot.admin.utils.storage.TencentCOSUtils;
import nexus.io.tio.boot.admin.vo.SystemTxCosConfigVo;
import nexus.io.tio.utils.crypto.Md5Utils;
import nexus.io.tio.utils.hutool.FilenameUtils;
import nexus.io.tio.utils.snowflake.SnowflakeIdUtils;

/**
 * Created by Tong Li
 */
@Slf4j
public class TencentStorageService implements StorageService {
  public RespBodyVo upload(UploadFile uploadFile) {

    String category = DEFAULT_CATEGORY;
    return upload(category, uploadFile);
  }

  public RespBodyVo upload(String category, UploadFile uploadFile) {
    if (StrKit.isBlank(category)) {
      category = DEFAULT_CATEGORY;
    }
    UploadResult vo = uploadFile(category, uploadFile);
    return RespBodyVo.ok(vo);
  }

  @Override
  public UploadResult uploadFile(String category, UploadFile uploadFile) {
    long id = SnowflakeIdUtils.id();
    UploadResult vo = uploadFile(category, uploadFile, id);
    return vo;
  }

  @Override
  public UploadResult uploadFile(String category, UploadFile uploadFile, Long id) {
    String filename = uploadFile.getName();

    String suffix = FilenameUtils.getSuffix(filename);
    String newFilename = id + "." + suffix;

    String targetName = category + newFilename;

    UploadResult vo = uploadFile(id, targetName, uploadFile, suffix);
    return vo;
  }

  public String getUrl(String bucketName, String targetName) {
    return TencentCOSUtils.getUrl(bucketName, targetName);
  }

  @Override
  public String getUrl(String targetName) {
    return TencentCOSUtils.getUrl(targetName);
  }

  public UploadResult getUrlById(String id) {
    return getUrlById(Long.parseLong(id));
  }

  public UploadResult getUrlById(long id) {
    Row record = Aop.get(SystemUploadFileDao.class).getFileBasicInfoById(id);
    if (record == null) {
      return null;
    }
    String target_name = record.getStr("target_name");
    String url = this.getUrl(record.getStr("bucket_name"), target_name);
    String originFilename = record.getStr("fielename");
    String md5 = record.getStr("md5");
    Long size = record.getLong("size");
    
    UploadResult uploadResult = new UploadResult(id, originFilename, size, url, md5);
    uploadResult.setTargetName(target_name);
    return uploadResult;
  }

  public UploadResult getUrlByMd5(String md5) {
    return Aop.get(SystemUploadFileService.class).getUrlByMd5(md5);
  }

  @Override
  public UploadResult uploadFile(long id, String targetName, UploadFile uploadFile, String suffix) {
    byte[] fileContent = uploadFile.getData();
    String filename = uploadFile.getName();
    long size = uploadFile.getSize();
    SystemTxCosConfigVo systemTxCosConfig = Aop.get(SysConfigConstantsService.class).getSystemTxCosConfig();

    String bucketName = systemTxCosConfig.getBucketName();

    String etag = null;
    COSClient cosClient = null;
    try {
      cosClient = TencentCOSUtils.buildClient();
      etag = TencentCOSUtils.upload(cosClient, targetName, fileContent, suffix).getETag();
    } catch (Exception e) {
      log.error("Error uploading file", e);
      throw new RuntimeException(e);
    } finally {
      if (cosClient != null) {
        cosClient.shutdown();
      }
    }

    // Log and save to database
    log.info("Uploaded to COS with ETag: {}", etag);
    String md5 = Md5Utils.md5Hex(fileContent);
    TableInput kv = TableInput.create().set("md5", md5).set("name", filename).set("size", size)
        //
        .set("platform", StoragePlatformConst.aws_s3).set("region_name", systemTxCosConfig.getRegion())
        //
        .set("bucket_name", bucketName).set("target_name", targetName).set("file_id", etag);

    ApiTable.save(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);
    String downloadUrl = getUrl(bucketName, targetName);

    UploadResult uploadResultVo = new UploadResult();
    uploadResultVo.setId(id).setUrl(downloadUrl).setSize(size);
    uploadResultVo.setMd5(md5).setTargetName(targetName);

    return uploadResultVo;
  }

  @Override
  public String getPresignedDownloadUrl(String targetName) {
    return TencentCOSUtils.getPresignedDownloadUrl(targetName);
  }

  @Override
  public String getPresignedDownloadUrl(String bucket, String targetName) {
    return TencentCOSUtils.getPresignedDownloadUrl(bucket, targetName);
  }

  @Override
  public String getPresignedDownloadUrl(String region, String bucket, String targetName) {
    return TencentCOSUtils.getPresignedDownloadUrl(region, bucket, targetName);
  }

  @Override
  public UploadResult getPresignedDownloadUrl(Long id) {
    return Aop.get(SystemUploadFileService.class).getPresignedDownloadUrl(id);
  }

}
