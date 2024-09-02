package com.litongjava.tio.boot.admin.services;

import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Record;
import com.litongjava.table.model.TableInput;
import com.litongjava.table.model.TableResult;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.admin.config.AwsS3Config;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.boot.admin.utils.AwsS3Utils;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.utils.resp.RespVo;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
@Slf4j
public class AwsS3StorageService {
  public RespVo upload(String category, UploadFile uploadFile) {
    if (StrKit.isBlank(category)) {
      category = "default";
    }
    String filename = uploadFile.getName();
    int size = uploadFile.getSize();
    byte[] fileContent = uploadFile.getData();

    return upload(category, filename, size, fileContent);
  }

  public RespVo upload(String category, String filename, int size, byte[] fileContent) {
    Kv kvResult = uploadReturnKv(category, filename, size, fileContent);
    return RespVo.ok(kvResult);
  }

  public Kv uploadReturnKv(String category, String filename, int size, byte[] fileContent) {
    // 上传文件
    long threadId = Thread.currentThread().getId();
    if (threadId > 31L) {
      threadId %= 31L;
    }

    if (threadId < 0L) {
      threadId = 0L;
    }
    long id = SnowflakeIdUtils.id();
    String suffix = FileNameUtil.getSuffix(filename);
    String newFilename = id + "." + suffix;

    String targetName = category + "/" + newFilename;

    Kv kvResult = uploadBytes(id, filename, targetName, fileContent, size, suffix);
    return kvResult;
  }

  public Kv uploadBytes(long id, String filename, String targetName, byte[] fileContent, int size, String suffix) {

    String etag = null;
    // 示例使用upload方法
    try (S3Client client = new AwsS3Config().buildClient();) {
      PutObjectResponse response = AwsS3Utils.upload(client, AwsS3Utils.bucketName, targetName, fileContent, suffix);
      etag = response.eTag();
    } catch (Exception e) {
      log.error("Error uploading file to Tencent COS", e);
      throw new RuntimeException(e);
    }

    // Log and save to database
    log.info("Uploaded with ETag: {}", etag);
    String md5 = MD5.create().digestHex(fileContent);

    TableInput kv = TableInput.create().set("md5", md5).set("filename", filename).set("file_size", size)
        //
        .set("platform", "aws s3").set("region_name", AwsS3Utils.regionName).set("bucket_name", AwsS3Utils.bucketName)
        //
        .set("target_name", targetName).set("file_id", etag);

    TableResult<Kv> save = ApiTable.save(TableNames.tio_boot_admin_system_upload_file, kv);
    String downloadUrl = getUrl(AwsS3Utils.bucketName, targetName);

    Kv kvResult = Kv.create().set("id", save.getData().get("id").toString()).set("url", downloadUrl);

    return kvResult;

  }

  public String getUrl(String bucketName, String targetName) {
    return String.format(AwsS3Utils.urlFormat, AwsS3Utils.bucketName, targetName);
  }

  public Kv getUrlById(String id) {
    String sql = "select md5,bucket_name,target_name from tio_boot_admin_system_upload_file where id=? and deleted=0";
    Record record = Db.findFirst(sql, Long.parseLong(id));
    if (record == null) {
      return null;
    }
    String url = this.getUrl(record.getStr("bucket_name"), record.getStr("target_name"));
    Kv kv = record.toKv();
    kv.remove("target_name");
    kv.remove("bucket_name");

    Kv retval = Kv.by("url", url);
    retval.set("id", kv.get("id").toString());
    return retval;
  }

  public Kv getUrlByMd5(String md5) {
    String sql = "select id,bucket_name,target_name from tio_boot_admin_system_upload_file where md5=? and deleted=0";
    Record record = Db.findFirst(sql, md5);
    if (record == null) {
      return null;
    }
    String url = this.getUrl(record.getStr("bucket_name"), record.getStr("target_name"));
    Kv kv = record.toKv();

    Kv retval = Kv.by("url", url);
    retval.set("id", kv.get("id").toString());
    return retval;
  }
}
