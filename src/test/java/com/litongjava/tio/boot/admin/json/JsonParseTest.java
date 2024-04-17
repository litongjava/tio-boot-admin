package com.litongjava.tio.boot.admin.json;

import com.alibaba.fastjson2.JSONObject;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.json.Json;
import org.junit.Test;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class JsonParseTest {

  @Test
  public void testParse() {
    String jsonString = "['url']";
    System.out.println(FastJson2Utils.parseArray(jsonString));
  }
}
