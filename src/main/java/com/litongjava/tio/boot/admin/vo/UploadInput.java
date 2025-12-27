package com.litongjava.tio.boot.admin.vo;

public class UploadInput {
  public String localFilePath, targetName;

  public UploadInput(String localFilePath, String targetName) {
    this.localFilePath = localFilePath;
    this.targetName = targetName;
  }
}
