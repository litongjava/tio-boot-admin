package com.litongjava.tio.boot.admin.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;

import com.litongjava.tio.boot.admin.vo.IpGeoInfo;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

public class IpDatabaseUtils {

  /**
   * 获取CityResponse，避免多次调用reader.city()进行重复查询
   */
  private static CityResponse getCityResponse(DatabaseReader reader, String ip) throws IOException, GeoIp2Exception {
    InetAddress ipAddress = InetAddress.getByName(ip);
    return reader.city(ipAddress);
  }

  public static String getCountry(DatabaseReader reader, String ip) throws Exception {
    CityResponse response = getCityResponse(reader, ip);
    return response.getCountry().getNames().get("zh-CN");
  }

  public static String getProvince(DatabaseReader reader, String ip) throws Exception {
    CityResponse response = getCityResponse(reader, ip);
    return response.getMostSpecificSubdivision().getNames().get("zh-CN");
  }

  public static String getCity(DatabaseReader reader, String ip) throws Exception {
    CityResponse response = getCityResponse(reader, ip);
    return response.getCity().getNames().get("zh-CN");
  }

  public static Double getLongitude(DatabaseReader reader, String ip) throws Exception {
    CityResponse response = getCityResponse(reader, ip);
    return response.getLocation().getLongitude();
  }

  public static Double getLatitude(DatabaseReader reader, String ip) throws Exception {
    CityResponse response = getCityResponse(reader, ip);
    return response.getLocation().getLatitude();
  }

  public static IpGeoInfo getGeoInfo(DatabaseReader reader, String ip) {
    CityResponse response;
    try {
      response = getCityResponse(reader, ip);
    } catch (IOException | GeoIp2Exception e) {
      throw new RuntimeException(e);
    }

    Double longitude = response.getLocation().getLongitude();
    Double latitude = response.getLocation().getLatitude();

    String city = response.getCity().getName();
    String country = response.getCountry().getName();
    String location = String.join(", ", Objects.toString(city, ""), Objects.toString(country, "")).replaceAll("(^,\\s*|,\\s*$)", "");

    return new IpGeoInfo(longitude, latitude, location);
  }

  public static IpGeoInfo getGeoInfo(String ip) {
    DatabaseReader reader = GeoLite2Utils.getDatabase();
    return getGeoInfo(reader, ip);
  }
}
