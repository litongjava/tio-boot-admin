package com.litongjava.tio.boot.admin.services;

import com.jfinal.kit.StrKit;
import com.litongjava.tio.boot.admin.vo.SystemTxCosConfigVo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.region.Region;

import java.io.InputStream;

/**
 * 腾讯云对象存储接口
 */
public class TencentCOSService {

  /**
   * 从输入流进行读取并上传到COS
   */
  public String simpleUploadFileFromStream(SystemTxCosConfigVo systemTxCosConfig, String key, String contentType,
                                           InputStream inputStream, long size) {
    // 1 初始化用户身份信息(secretId, secretKey)
    COSCredentials cred = new BasicCOSCredentials(systemTxCosConfig.getSecretId(), systemTxCosConfig.getSecretKey());
    // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
    ClientConfig clientConfig = new ClientConfig(new Region(systemTxCosConfig.getRegion()));
    // 3 生成cos客户端
    COSClient cosclient = new COSClient(cred, clientConfig);
    // bucket名需包含appid
    String bucketName = systemTxCosConfig.getBucketName();
    // String key = "aaa/bbb.jpg";
    // InputStream input = new ByteArrayInputStream(new byte[10]);
    ObjectMetadata objectMetadata = new ObjectMetadata();
    // 从输入流上传必须制定content length, 否则http客户端可能会缓存所有数据，存在内存OOM的情况
    objectMetadata.setContentLength(size);
    // 默认下载时根据cos路径key的后缀返回响应的contenttype, 上传时设置contenttype会覆盖默认值
    if (!StrKit.isBlank(contentType)) {
      objectMetadata.setContentType(contentType);
    }
    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, objectMetadata);
    // 设置存储类型, 默认是标准(Standard), 低频(standard_ia)
    putObjectRequest.setStorageClass(StorageClass.Standard);
    try {
      PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);
      // logger.info("putObjectResult:" + GsonTools.toJson(putObjectResult));
      // putobjectResult会返回文件的etag
      return putObjectResult.getETag();
    } catch (CosClientException e) {
      e.printStackTrace();
    } finally {
      // 关闭客户端
      cosclient.shutdown();
    }
    return null;
  }

}