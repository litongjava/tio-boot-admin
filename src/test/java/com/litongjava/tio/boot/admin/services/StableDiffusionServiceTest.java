package com.litongjava.tio.boot.admin.services;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jfinal.kit.Kv;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.config.DbConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class StableDiffusionServiceTest {

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.runWith(DbConfig.class);
  }

  @Test
  public void saveToDb() {
    String url = "https://firebasestorage.googleapis.com/v0/b/imaginix-eda2e.appspot.com/o/public%2Fimages%2F369047325126995968.jpg?alt=media";
    Kv srcImage = Kv.create().set("id", 1).set("ur", url);
    Kv dstImage = Kv.create().set("id", 1).set("ur", url);

    //{mode=image-to-image, output_format=jpeg, strength=1, model=sd3,
    // prompt=Lighthouse on a cliff overlooking the ocean}
    Map<String, Object> requestMap = new HashMap<>();
    requestMap.put("mode", "image-to-image");
    requestMap.put("output_format", "jpeg");
    requestMap.put("strength", 1);
    requestMap.put("model", "sd3");
    requestMap.put("prompt", "Lighthouse on a cliff overlooking the ocean");

    Aop.get(StableDiffusionService.class).saveToDb(requestMap,srcImage,dstImage);


  }
}