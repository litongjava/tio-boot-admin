package nexus.io.tio.boot.admin.services.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;

import lombok.extern.slf4j.Slf4j;
import nexus.io.model.upload.UploadResult;
import nexus.io.tio.boot.admin.consts.StoragePlatformConst;
import nexus.io.tio.boot.admin.utils.storage.AliyunOssUtils;
import nexus.io.tio.boot.admin.utils.storage.AwsS3Utils;
import nexus.io.tio.boot.admin.utils.storage.CloudflareR2Utils;
import nexus.io.tio.boot.admin.utils.storage.TencentCOSUtils;
import nexus.io.tio.boot.admin.vo.UploadInput;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
public class StorageUploadService {

  public UploadResult uploadFile(String storagePlatform, String localFile, String targetName) {
    UploadResult uploadResultVo = new UploadResult();
    String downloadUrl = null;
    String etag = null;
    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      PutObjectResponse response = AwsS3Utils.upload(AwsS3Utils.bucketName, targetName, new File(localFile));
      etag = response.eTag();
      downloadUrl = AwsS3Utils.getUrl(AwsS3Utils.bucketName, targetName);
      uploadResultVo.setEtag(etag).setUrl(downloadUrl);

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      PutObjectResult response = AliyunOssUtils.upload(AliyunOssUtils.bucketName, targetName, new File(localFile));
      etag = response.getETag();
      downloadUrl = AliyunOssUtils.getUrl(AliyunOssUtils.bucketName, targetName);
      uploadResultVo.setEtag(etag).setUrl(downloadUrl);

    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {

      com.qcloud.cos.model.PutObjectResult response = TencentCOSUtils.upload(TencentCOSUtils.bucketName, targetName,
          new File(localFile));
      etag = response.getETag();
      downloadUrl = AliyunOssUtils.getUrl(TencentCOSUtils.bucketName, targetName);
      uploadResultVo.setEtag(etag).setUrl(downloadUrl);

    } else if (StoragePlatformConst.cloudflare_r2.equals(storagePlatform)) {
      PutObjectResponse response = CloudflareR2Utils.upload(CloudflareR2Utils.bucketName, targetName,
          new File(localFile));
      etag = response.eTag();
      downloadUrl = AliyunOssUtils.getUrl(CloudflareR2Utils.bucketName, targetName);
      uploadResultVo.setEtag(etag).setUrl(downloadUrl);

    }

    return uploadResultVo;
  }

  public List<UploadResult> uploadFile(String storagePlatform, List<UploadInput> uploadFiles) {
    List<UploadResult> result = new ArrayList<>(uploadFiles.size());

    String downloadUrl = null;
    String etag = null;
    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      try (S3Client client = AwsS3Utils.buildClient();) {
        for (int i = 0; i < uploadFiles.size(); i++) {
          UploadInput uploadInput = uploadFiles.get(i);
          PutObjectResponse response = AwsS3Utils.upload(uploadInput.targetName, new File(uploadInput.localFilePath));
          etag = response.eTag();
          downloadUrl = AwsS3Utils.getUrl(uploadInput.targetName);
          UploadResult uploadResultVo = new UploadResult(etag, downloadUrl);
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
          PutObjectResult response = AliyunOssUtils.upload(uploadInput.targetName, new File(uploadInput.localFilePath));
          etag = response.getETag();
          downloadUrl = AliyunOssUtils.getUrl(uploadInput.targetName);
          UploadResult uploadResultVo = new UploadResult(etag, downloadUrl);
          result.add(uploadResultVo);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      try {
        for (int i = 0; i < uploadFiles.size(); i++) {
          UploadInput uploadInput = uploadFiles.get(i);
          
          com.qcloud.cos.model.PutObjectResult response = TencentCOSUtils.upload(uploadInput.targetName, new File(uploadInput.localFilePath));
          etag = response.getETag();
          downloadUrl = TencentCOSUtils.getUrl(uploadInput.targetName);
          UploadResult uploadResultVo = new UploadResult(etag, downloadUrl);
          result.add(uploadResultVo);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
      
    } else if (StoragePlatformConst.cloudflare_r2.equals(storagePlatform)) {
      try {
        for (int i = 0; i < uploadFiles.size(); i++) {
          UploadInput uploadInput = uploadFiles.get(i);
          
          
          PutObjectResponse response = CloudflareR2Utils.upload(uploadInput.targetName, new File(uploadInput.localFilePath));
          etag = response.eTag();
          downloadUrl = CloudflareR2Utils.getUrl(uploadInput.targetName);
          UploadResult uploadResultVo = new UploadResult(etag, downloadUrl);
          result.add(uploadResultVo);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
    return result;
  }

}
