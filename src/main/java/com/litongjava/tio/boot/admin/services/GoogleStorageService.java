package com.litongjava.tio.boot.admin.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.data.utils.SnowflakeIdGenerator;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.activerecord.Db;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.resp.RespVo;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
@Slf4j
public class GoogleStorageService {
  String bucketName = EnvUtils.getStr("BUCKET_NAME");

  public RespVo uploadImageToGoogle(UploadFile uploadFile) {
    String filename = uploadFile.getName();
    String suffix = FileNameUtil.getSuffix(filename);
    String contentType = ContentTypeUtils.getContentType(suffix);

    byte[] fileContent = uploadFile.getData();
    //int size = uploadFile.getSize();

    return uploadImageBytes(fileContent, filename, suffix, contentType);
  }

  public RespVo uploadImageBytes(byte[] fileContent, String filename, String suffix, String contentType) {
    return uploadBytes(fileContent, filename, suffix, "public/images", contentType);
  }

  public RespVo uploadBytes(byte[] fileContent, String filename, String suffix, String folderName,
                            String contentType) {

    // 上传文件
    long threadId = Thread.currentThread().getId();
    if (threadId > 31L) {
      threadId %= 31L;
    }

    if (threadId < 0L) {
      threadId = 0L;
    }
    long id = (new SnowflakeIdGenerator(threadId, 0L)).generateId();

    String newFilename = id + "." + suffix;

    String targetName = folderName + "/" + newFilename;


    uploadBytesToGoogle(fileContent, targetName, contentType);

    // 存入到数据库
    String md5 = MD5.create().digestHex(fileContent);
    Kv kv = Kv.create();
    kv.set("md5", md5);
    kv.set("filename", filename);
    kv.set("file_size", fileContent.length);
    kv.set("platform", "google");
    kv.set("bucket_name", bucketName);
    String replaceTargetName = replaceTargetName(targetName);
    kv.set("target_name", replaceTargetName);
    kv.set("file_id", id);

    DbJsonBean<Kv> save = Aop.get(DbJsonService.class).save(TableNames.tio_boot_admin_system_upload_file, kv);

    // 下载地址
    String downloadUrl = getUrl(bucketName, replaceTargetName);
    Kv kv1 = Kv.create();
    kv1.set("id", save.getData().get("id") + "");
    kv1.set("url", downloadUrl);

    // 返回RespVo
    return RespVo.ok(kv1);
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
    String sql = "select bucket_name,target_name from " + TableNames.tio_boot_admin_system_upload_file + " where id=?";
    Record record = Db.findFirst(sql, fileId);

    return getUrl(record.getStr("bucket_name"), record.getStr("target_name"));
  }

  public String getUrl(String name, String targetName) {

    String template = "https://firebasestorage.googleapis.com/v0/b/%s.appspot.com/o/%s?alt=media";
    return String.format(template, name, targetName);
  }
}
