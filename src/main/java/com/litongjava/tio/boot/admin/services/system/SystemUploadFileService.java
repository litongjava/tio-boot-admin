package com.litongjava.tio.boot.admin.services.system;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.upload.UploadResult;
import com.litongjava.tio.boot.admin.consts.StoragePlatformConst;
import com.litongjava.tio.boot.admin.dao.SystemUploadFileDao;
import com.litongjava.tio.boot.admin.utils.AliyunOssUtils;
import com.litongjava.tio.boot.admin.utils.AwsS3Utils;
import com.litongjava.tio.boot.admin.utils.CloudflareR2Utils;
import com.litongjava.tio.boot.admin.utils.TencentCOSUtils;

public class SystemUploadFileService {

  public String getUrl(String platform, String region_name, String bucket_name, String targetName) {
    if (StoragePlatformConst.aws_s3.equals(platform)) {
      return AwsS3Utils.getUrl(region_name, bucket_name, targetName);

    } else if (StoragePlatformConst.aliyun_oss.equals(platform)) {
      return AliyunOssUtils.getUrl(region_name, bucket_name, targetName);

    } else if (StoragePlatformConst.tencent_cos.equals(platform)) {
      return TencentCOSUtils.getUrl(region_name, bucket_name, targetName);

    } else if (StoragePlatformConst.cloudflare_r2.equals(platform)) {
      return CloudflareR2Utils.getUrl(bucket_name, targetName);

    } else {
      return String.format(AwsS3Utils.urlFormat, AwsS3Utils.bucketName, region_name, targetName);

    }
  }

  public String getPresignedDownloadUrl(String platform, String region_name, String bucket_name, String targetName,
      String downloadFilename) {
    if (StoragePlatformConst.aws_s3.equals(platform)) {
      return AwsS3Utils.getPresignedDownloadUrl(region_name, bucket_name, targetName, downloadFilename);

    } else if (StoragePlatformConst.aliyun_oss.equals(platform)) {
      return AliyunOssUtils.getPresignedDownloadUrl(region_name, bucket_name, targetName, downloadFilename);

    } else if (StoragePlatformConst.tencent_cos.equals(platform)) {
      return TencentCOSUtils.getPresignedDownloadUrl(region_name, bucket_name, targetName, downloadFilename);

    } else if (StoragePlatformConst.cloudflare_r2.equals(platform)) {
      return CloudflareR2Utils.getPresignedDownloadUrl(region_name, bucket_name, targetName, downloadFilename);

    } else {
      return AwsS3Utils.getPresignedDownloadUrl(region_name, bucket_name, targetName, downloadFilename);

    }
  }

  public UploadResult getUrlById(String id) {
    return getUrlById(Long.parseLong(id));
  }

  public UploadResult getUrlById(long id) {
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
    return new UploadResult(id, originFilename, size, url, md5);
  }

  public UploadResult getPresignedDownloadUrl(Long id) {
    Row record = Aop.get(SystemUploadFileDao.class).getFileBasicInfoById(id);
    if (record == null) {
      return null;
    }
    String platform = record.getStr("platform");
    String region_name = record.getStr("region_name");
    String bucket_name = record.getStr("bucket_name");
    String target_name = record.getStr("target_name");
    String name = record.getStr("name");

    String url = this.getPresignedDownloadUrl(platform, region_name, bucket_name, target_name, name);
    String originFilename = record.getStr("fielename");
    String md5 = record.getStr("md5");
    Long size = record.getLong("size");
    return new UploadResult(id, originFilename, size, url, md5);
  }

  public UploadResult getUrlByMd5(String md5) {
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
    return new UploadResult(id, originFilename, size, url, md5);
  }

  public List<UploadResult> getUploadResultByIds(List<Long> ids) {
    List<Row> rows = Aop.get(SystemUploadFileDao.class).getFileBasicInfoByIds(ids);
    if (rows == null) {
      return null;
    }
    List<UploadResult> results = new ArrayList<>();

    for (Row record : rows) {
      Long id = record.getLong("id");
      String platform = record.getStr("platform");
      String region_name = record.getStr("region_name");
      String bucket_name = record.getStr("bucket_name");
      String target_name = record.getStr("target_name");

      String url = this.getUrl(platform, region_name, bucket_name, target_name);
      String originFilename = record.getStr("fielename");
      String md5 = record.getStr("md5");
      Long size = record.getLong("size");
      UploadResult uploadResultVo = new UploadResult(id, originFilename, size, url, md5);
      results.add(uploadResultVo);
    }
    return results;
  }
}
