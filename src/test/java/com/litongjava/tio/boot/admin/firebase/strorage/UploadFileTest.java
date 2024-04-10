package com.litongjava.tio.boot.admin.firebase.strorage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import com.litongjava.tio.utils.hutool.ResourceUtil;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class UploadFileTest {

  @Test
  public void getResourceAsStream() {
    InputStream resourceAsStream = ResourceUtil.getResourceAsStream("imaginix.json");
    System.out.println(resourceAsStream);
  }


  @Test
  public void testUploadFile() throws IOException {

    InputStream serviceAccount = ResourceUtil.getResourceAsStream("imaginix.json");

    FirebaseOptions options = new FirebaseOptions.Builder()
      .setCredentials(GoogleCredentials.fromStream(serviceAccount))
      .setStorageBucket("imaginix-eda2e.appspot.com")
      .build();

    FirebaseApp.initializeApp(options);

    Bucket bucket = StorageClient.getInstance().bucket();


    String localFileName = "F:\\my_file\\my_photo\\kitty\\kitty-cat.png";
    Path path = Paths.get(localFileName);
    byte[] fileContent = Files.readAllBytes(path);

    String targetName = "public/images/001.png";
    BlobId blobId = BlobId.of(bucket.getName(), targetName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();

    Storage storage = bucket.getStorage();

    System.out.println(blobInfo);
    Blob blob = storage.create(blobInfo, fileContent);
    System.out.println(blob);


  }
}
