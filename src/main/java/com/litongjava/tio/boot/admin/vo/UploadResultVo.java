package com.litongjava.tio.boot.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UploadResultVo {
  private Long id;
  private Long size;
  private String name, targetName, url, md5;
  private String content;

  public UploadResultVo(long id, String filename, Long size, String url, String md5) {
    this.id = id;
    this.name = filename;
    this.size = size;
    this.url = url;
    this.md5 = md5;
  }
  
  public UploadResultVo(long id, String filename, String url, String md5) {
    this.id = id;
    this.name = filename;
    this.url = url;
    this.md5 = md5;
  }

  public UploadResultVo(long id, String filename, String targetName, String url, String md5) {
    this.id = id;
    this.name = filename;
    this.targetName = targetName;
    this.url = url;
    this.md5 = md5;
  }
}
