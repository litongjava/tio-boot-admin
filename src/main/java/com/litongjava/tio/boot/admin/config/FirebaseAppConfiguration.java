package com.litongjava.tio.boot.admin.config;

import java.io.IOException;
import java.io.InputStream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.litongjava.annotation.AConfiguration;
import com.litongjava.annotation.Initialization;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.hutool.ResourceUtil;

@AConfiguration
public class FirebaseAppConfiguration {

  @Initialization
  public void config() throws IOException {
    InputStream serviceAccount = ResourceUtil.getResourceAsStream("google_firebase.json");

    String bucketName = EnvUtils.getStr("BUCKET_NAME");

    FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(
        //
        GoogleCredentials.fromStream(serviceAccount)).setStorageBucket(bucketName + ".appspot.com").build();

    FirebaseApp.initializeApp(options);
    TioBootServer.me().addDestroyMethod(() -> {
      FirebaseApp.getInstance().delete();
    });
  }
}
