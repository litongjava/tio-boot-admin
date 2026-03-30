package com.litongjava.tio.boot.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 腾讯云对象存储配置信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemTxCosConfigVo {
  private String domain;
  private String region;
  private String bucketName;
  private String secretId;
  private String secretKey;

}