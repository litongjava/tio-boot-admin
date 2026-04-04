package com.litongjava.tio.boot.admin.utils;

import lombok.extern.slf4j.Slf4j;
import nexus.io.tio.utils.environment.EnvUtils;
import nexus.io.tio.utils.hutool.StrUtil;

@Slf4j
public class AwsProfileUtils {

  public static String jaasConfig() {
    String awsProfile = firstNonBlank(EnvUtils.get("AWS_PROFILE"), EnvUtils.get("aws.profile"),
        EnvUtils.get("aws.msk.aws-profile"));

    describeAuthMode(awsProfile);
    String jaasConfig = buildJaasConfig(awsProfile);
    return jaasConfig;
  }

  private static String buildJaasConfig(String awsProfile) {
    if (StrUtil.isNotBlank(awsProfile)) {
      log.info("AWS_PROFILE detected for MSK IAM auth: {}", awsProfile);
      return "software.amazon.msk.auth.iam.IAMLoginModule required " + "awsProfileName=\"" + escapeJaasValue(awsProfile)
          + "\";";
    }

    log.info("AWS_PROFILE not found, using default AWS credentials provider chain for MSK IAM auth");
    return "software.amazon.msk.auth.iam.IAMLoginModule required;";
  }

  private static String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      if (StrUtil.isNotBlank(value)) {
        return value.trim();
      }
    }
    return null;
  }

  private static String escapeJaasValue(String value) {
    if (value == null) {
      return null;
    }
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static String describeAuthMode(String awsProfile) {
    if (StrUtil.isNotBlank(awsProfile)) {
      return "AWS_PROFILE(" + awsProfile + ")";
    }
    return "DEFAULT_CREDENTIALS_CHAIN";
  }

}
