package com.litongjava.tio.boot.admin.services.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.litongjava.tio.boot.admin.consts.StoragePlatformConst;
import com.litongjava.tio.boot.admin.utils.AliyunOssUtils;
import com.litongjava.tio.boot.admin.utils.AwsS3Utils;
import com.litongjava.tio.boot.admin.vo.UploadInput;
import com.litongjava.tio.boot.admin.vo.UploadResultVo;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
public class StorageUploadService {

  public UploadResultVo uploadFile(String storagePlatform, String localFile, String targetName) {
    UploadResultVo uploadResultVo = new UploadResultVo();
    String downloadUrl = null;
    String etag = null;
    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      try (S3Client client = AwsS3Utils.buildClient();) {
        PutObjectResponse response = AwsS3Utils.upload(client, AwsS3Utils.bucketName, targetName, new File(localFile));
        etag = response.eTag();
        downloadUrl = AwsS3Utils.getUrl(AwsS3Utils.bucketName, targetName);
        uploadResultVo.setEtag(etag).setUrl(downloadUrl);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      OSS client = null;
      try {
        client = AliyunOssUtils.buildClient();
        PutObjectResult response = AliyunOssUtils.upload(client, AliyunOssUtils.bucketName, targetName, new File(localFile));
        etag = response.getETag();
        downloadUrl = AliyunOssUtils.getUrl(AliyunOssUtils.bucketName, targetName);
        uploadResultVo.setEtag(etag).setUrl(downloadUrl);

      } catch (Exception e) {
        log.error(e.getMessage(), e);
      } finally {
        if (client != null) {
          client.shutdown();
        }
      }

    }

    return uploadResultVo;
  }

  public List<UploadResultVo> uploadFile(String storagePlatform, List<UploadInput> uploadFiles) {
    List<UploadResultVo> result = new ArrayList<>(uploadFiles.size());

    String downloadUrl = null;
    String etag = null;
    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      try (S3Client client = AwsS3Utils.buildClient();) {
        for (int i = 0; i < uploadFiles.size(); i++) {
          UploadInput uploadInput = uploadFiles.get(i);
          PutObjectResponse response = AwsS3Utils.upload(client, uploadInput.targetName, new File(uploadInput.localFilePath));
          etag = response.eTag();
          downloadUrl = AwsS3Utils.getUrl(uploadInput.targetName);
          UploadResultVo uploadResultVo = new UploadResultVo(etag, downloadUrl);
          result.add(uploadResultVo);
        }

      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      OSS client = null;
      try {
        for (int i = 0; i < uploadFiles.size(); i++) {
          UploadInput uploadInput = uploadFiles.get(i);
          client = AliyunOssUtils.buildClient();
          PutObjectResult response = AliyunOssUtils.upload(client, uploadInput.targetName, new File(uploadInput.localFilePath));
          etag = response.getETag();
          downloadUrl = AliyunOssUtils.getUrl(uploadInput.targetName);
          UploadResultVo uploadResultVo = new UploadResultVo(etag, downloadUrl);
          result.add(uploadResultVo);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      } finally {
        if (client != null) {
          client.shutdown();
        }
      }

    }
    return result;
  }

}
