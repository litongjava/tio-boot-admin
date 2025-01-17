package com.litongjava.tio.boot.admin.vo;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RssChannelItem {
  private String guid;
  private String title;
  private String description;
  private Date pubDate;
  private String link;
  private String author;
  private List<String> categores;
}
