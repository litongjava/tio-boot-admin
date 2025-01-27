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
  private String filename, targetName, url, md5;

  public UploadResultVo(long id, String filename, Long size, String url, String md5) {
    this.id = id;
    this.filename = filename;
    this.size = size;
    this.url = url;
    this.md5 = md5;
  }
}
