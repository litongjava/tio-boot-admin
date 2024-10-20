package com.litongjava.tio.boot.admin.services;

import java.io.ByteArrayInputStream;

import com.jfinal.kit.Kv;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.db.activerecord.Record;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.dao.SystemUploadFileDao;
import com.litongjava.tio.boot.admin.vo.SystemTxCosConfigVo;
import com.litongjava.tio.boot.admin.vo.UploadResultVo;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdGenerator;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
@Slf4j
public class TencentStorageService implements StorageService {
  public RespBodyVo upload(UploadFile uploadFile) {
    String filename = uploadFile.getName();
    int size = uploadFile.getSize();
    byte[] fileContent = uploadFile.getData();

    // 上传文件
    long threadId = Thread.currentThread().getId();
    if (threadId > 31L) {
      threadId %= 31L;
    }

    if (threadId < 0L) {
      threadId = 0L;
    }
    long id = (new SnowflakeIdGenerator(threadId, 0L)).generateId();
    String suffix = FileNameUtil.getSuffix(filename);
    String newFilename = id + "." + suffix;

    String targetName = "public/" + newFilename;

    return uploadBytes(filename, id, targetName, fileContent, size, suffix);
  }

  public RespBodyVo uploadBytes(String filename, long id, String targetName, byte[] fileContent, int size, String suffix) {
    SystemTxCosConfigVo systemTxCosConfig = Aop.get(SysConfigConstantsService.class).getSystemTxCosConfig();

    COSClient cosClient = getCosClient(systemTxCosConfig);
    String bucketName = systemTxCosConfig.getBucketName();

    String etag = null;
    try {
      etag = upload(cosClient, bucketName, targetName, fileContent, suffix);
    } catch (Exception e) {
      log.error("Error uploading file to Tencent COS", e);
      return RespBodyVo.fail(e.getMessage());
    } finally {
      cosClient.shutdown();
    }

    // Log and save to database
    log.info("Uploaded to COS with ETag: {}", etag);
    String md5 = MD5.create().digestHex(fileContent);
    TableInput kv = TableInput.create().set("md5", md5).set("filename", filename).set("file_size", size).set("platform", "tencent").set("region_name", systemTxCosConfig.getRegion())
        .set("bucket_name", bucketName).set("target_name", targetName).set("file_id", etag);

    TableResult<Kv> save = ApiTable.save(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);
    String downloadUrl = getUrl(bucketName, targetName);

    Kv kvResult = Kv.create().set("id", save.getData().get("id").toString()).set("url", downloadUrl);

    return RespBodyVo.ok(kvResult);

  }

  /**
   * upload
   */
  private String upload(COSClient cosClient, String bucketName, String targetName, byte[] fileContent, String suffix) {
    PutObjectRequest putObjectRequest = getPutObjectRequest(targetName, fileContent, suffix, bucketName);
    PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
    return putObjectResult.getETag();
  }

  /**
   * getPutObjectRequest
   */
  private PutObjectRequest getPutObjectRequest(String targetName, byte[] fileContent, String suffix, String bucketName) {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileContent);
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(fileContent.length);
    objectMetadata.setContentType(ContentTypeUtils.getContentType(suffix));

    return new PutObjectRequest(bucketName, targetName, byteArrayInputStream, objectMetadata);
  }

  /**
   * getCosClient
   */
  private COSClient getCosClient(SystemTxCosConfigVo systemTxCosConfig) {
    COSCredentials cred = new BasicCOSCredentials(systemTxCosConfig.getSecretId(), systemTxCosConfig.getSecretKey());
    ClientConfig clientConfig = new ClientConfig(new Region(systemTxCosConfig.getRegion()));
    return new COSClient(cred, clientConfig);
  }

  public String getUrl(String bucketName, String targetName) {
    SystemTxCosConfigVo systemTxCosConfig = Aop.get(SysConfigConstantsService.class).getSystemTxCosConfig();
    String region = systemTxCosConfig.getRegion();
    String domain = "https://" + bucketName + ".cos." + region + ".myqcloud.com";
    return domain + "/" + targetName;
  }

  public UploadResultVo getUrlById(String id) {
    return getUrlById(Long.parseLong(id));
  }

  public UploadResultVo getUrlById(long id) {
    Record record = Aop.get(SystemUploadFileDao.class).getFileBasicInfoById(id);
    if (record == null) {
      return null;
    }
    String url = this.getUrl(record.getStr("bucket_name"), record.getStr("target_name"));
    String originFilename = record.getStr("fielename");
    String md5 = record.getStr("md5");
    return new UploadResultVo(id, originFilename, url, md5);
  }

  public UploadResultVo getUrlByMd5(String md5) {
    Record record = Aop.get(SystemUploadFileDao.class).getFileBasicInfoByMd5(md5);
    if (record == null) {
      return null;
    }
    Long id = record.getLong("id");
    String url = this.getUrl(record.getStr("bucket_name"), record.getStr("target_name"));
    Kv kv = record.toKv();
    kv.set("url", url);
    kv.set("md5", md5);
    String originFilename = record.getStr("filename");
    return new UploadResultVo(id, originFilename, url, md5);
  }

  @Override
  public RespBodyVo upload(String category, UploadFile uploadFile) {
    return null;
  }

  @Override
  public UploadResultVo uploadBytes(String category, String originFilename, int size, byte[] fileContent) {
    return null;
  }

  @Override
  public UploadResultVo uploadBytes(long id, String originFilename, String targetName, byte[] fileContent, int size, String suffix) {
    return null;
  }

  
}
