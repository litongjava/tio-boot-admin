package com.litongjava.tio.boot.admin.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import com.jfinal.kit.Kv;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.upload.UploadFile;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.utils.crypto.Md5Utils;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
@Slf4j
public class GoogleStorageService {
  String bucketName = EnvUtils.getStr("BUCKET_NAME");

  public RespBodyVo uploadImageToGoogle(UploadFile uploadFile) {
    String filename = uploadFile.getName();
    String suffix = FilenameUtils.getSuffix(filename);
    String contentType = ContentTypeUtils.getContentType(suffix);

    byte[] fileContent = uploadFile.getData();
    // int size = uploadFile.getSize();

    return uploadImageBytes(fileContent, filename, suffix, contentType);
  }

  public RespBodyVo uploadImageBytes(byte[] fileContent, String filename, String suffix, String contentType) {
    return uploadBytes(fileContent, filename, suffix, "public/images", contentType);
  }

  public RespBodyVo uploadBytes(byte[] fileContent, String filename, String suffix, String folderName, String contentType) {

    // 上传文件
    long threadId = Thread.currentThread().getId();
    if (threadId > 31L) {
      threadId %= 31L;
    }

    if (threadId < 0L) {
      threadId = 0L;
    }
    long id = SnowflakeIdUtils.id();

    String newFilename = id + "." + suffix;

    String targetName = folderName + "/" + newFilename;

    uploadBytesToGoogle(fileContent, targetName, contentType);

    // 存入到数据库
    String md5 = Md5Utils.md5Hex(fileContent);
    TableInput kv = TableInput.create();
    kv.set("md5", md5);
    kv.set("filename", filename);
    kv.set("file_size", fileContent.length);
    kv.set("platform", "google");
    kv.set("bucket_name", bucketName);
    String replaceTargetName = replaceTargetName(targetName);
    kv.set("target_name", replaceTargetName);
    kv.set("file_id", id);

    TableResult<Kv> save = ApiTable.save(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);

    // 下载地址
    String downloadUrl = getUrl(bucketName, replaceTargetName);
    Kv kv1 = Kv.create();
    kv1.set("id", save.getData().get("id") + "");
    kv1.set("url", downloadUrl);

    // 返回RespVo
    return RespBodyVo.ok(kv1);
  }

  public Blob uploadBytesToGoogle(byte[] fileContent, String targetName, String contentType) {
    Bucket bucket = StorageClient.getInstance().bucket();
    BlobId blobId = BlobId.of(bucket.getName(), targetName);

    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
    Storage storage = bucket.getStorage();
    Blob blob = storage.create(blobInfo, fileContent);
    log.info("blob:{}", blob);
    return blob;

  }

  private String replaceTargetName(String targetName) {
    return targetName.replace("/", "%2F");
  }

  public String getUrlByFileId(long fileId) {
    String sql = "select bucket_name,target_name from " + TioBootAdminTableNames.tio_boot_admin_system_upload_file + " where id=?";
    Row record = Db.findFirst(sql, fileId);

    return getUrl(record.getStr("bucket_name"), record.getStr("target_name"));
  }

  public String getUrl(String name, String targetName) {

    String template = "https://firebasestorage.googleapis.com/v0/b/%s.appspot.com/o/%s?alt=media";
    return String.format(template, name, targetName);
  }
}
