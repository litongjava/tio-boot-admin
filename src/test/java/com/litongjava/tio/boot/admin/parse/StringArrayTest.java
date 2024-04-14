package com.litongjava.tio.boot.admin.parse;

import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.json.Json;
import org.junit.Test;

import java.util.Map;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class StringArrayTest {

  @Test
  public void testStringToArray() {
    String bodyString = "{\"key\":\"colors\",\"value\":[\"red\",\"orange\"]}";
    Map<String, Object> map = Json.getJson().parseToMap(bodyString, String.class, Object.class);

    String valueType = "string[]";
    Object value = map.get("value");
    if(value instanceof com.alibaba.fastjson2.JSONArray){

    }
//    for (Map.Entry<String, Object> e : map.entrySet()) {
//      System.out.println(e.getKey().getClass().toString());
//      System.out.println(e.getValue().getClass().toString());
//    }


  }
}
