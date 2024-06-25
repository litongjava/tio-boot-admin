package com.litongjava.tio.boot.admin.t2j;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class TimeTest {

  @Test
  public void test() {
    String[] updateTimes = { "2024-06-24 00:00:00", "2024-06-25 00:00:00" };

    // Parse the input strings to LocalDateTime
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime localStartTime = LocalDateTime.parse(updateTimes[0], formatter);
    LocalDateTime localEndTime = LocalDateTime.parse(updateTimes[1], formatter);

    // Convert LocalDateTime to OffsetDateTime with the desired offset, e.g., UTC
    OffsetDateTime startTime = localStartTime.atOffset(ZoneOffset.UTC);
    OffsetDateTime endTime = localEndTime.atOffset(ZoneOffset.UTC);

    // Format the output to include seconds
    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    System.out.println(startTime.format(outputFormatter));
    System.out.println(endTime.format(outputFormatter));
  }
}
