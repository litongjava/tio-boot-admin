package com.litongjava.tio.boot.admin.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.litongjava.jfinal.aop.annotation.ABean;
import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.utils.environment.EnvironmentUtils;
import com.litongjava.tio.utils.hutool.ResourceUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
@AConfiguration
public class FirebaseAppConfiguration {

  @AInitialization
  public void config() throws IOException {
    InputStream serviceAccount = ResourceUtil.getResourceAsStream("imaginix.json");

    String bucketName = EnvironmentUtils.getStr("BUCKET_NAME");

    FirebaseOptions options = new FirebaseOptions.Builder()
      .setCredentials(GoogleCredentials.fromStream(serviceAccount))
      .setStorageBucket(bucketName + ".appspot.com")
      .build();

    FirebaseApp.initializeApp(options);
    TioBootServer.me().addDestroyMethod(() -> {
      FirebaseApp.getInstance().delete();
    });
  }
}
