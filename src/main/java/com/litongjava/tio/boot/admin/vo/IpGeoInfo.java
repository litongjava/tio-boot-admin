package com.litongjava.tio.boot.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpGeoInfo {
  private Double longitude;
  private Double latitude;
  private String location;
}