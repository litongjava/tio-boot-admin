package nexus.io.tio.boot.admin.utils.storage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import nexus.io.tio.boot.admin.services.storage.UniStorageService;
import nexus.io.tio.boot.admin.services.system.SystemUploadFileService;
import nexus.io.tio.utils.environment.EnvUtils;
import software.amazon.awssdk.services.s3.S3Client;

/** Explicit opt-in integration check against a configured MinIO server. */
public class MinioStorageIntegrationCheck {
  public static void main(String[] args) throws Exception {
    EnvUtils.load();
    EnvUtils.set("app.storage.platform", "minio");
    String bucket = "tio-minio-check-" + System.currentTimeMillis();
    EnvUtils.set("MINIO_BUCKET_NAME", bucket);
    String key = "checks/中文 +#?.txt";
    byte[] bytes = "MinIO round trip".getBytes(StandardCharsets.UTF_8);
    S3Client client = MinioUtils.buildClient();
    Path local = Files.createTempFile("minio-check-", ".txt");
    boolean created = false;
    try {
      client.createBucket(b -> b.bucket(bucket));
      created = true;
      String etag = UniStorageUtils.upload(key, bytes, "txt");
      require(etag != null && !etag.isEmpty(), "Missing ETag");
      require(bucket.equals(UniStorageUtils.getBucketName()), "Wrong bucket");
      require("us-east-1".equals(UniStorageUtils.getRegionName()), "Wrong region");
      UniStorageService service = new UniStorageService();
      verifyDownload(service.getPresignedDownloadUrl(key), bytes);
      verifyDownload(service.getPresignedDownloadUrl(bucket, key), bytes);
      verifyDownload(service.getPresignedDownloadUrl("us-east-1", bucket, key), bytes);
      SystemUploadFileService records = new SystemUploadFileService();
      verifyDownload(records.getPresignedDownloadUrl("minio", "us-east-1", bucket, key, "example.txt"), bytes);
      require(records.getUrl("minio", "us-east-1", bucket, key).equals(service.getUrl(key)), "Wrong record URL");
      URI publicUri = URI.create(service.getUrl(bucket, key));
      require(publicUri.getPath().equals("/" + bucket + "/" + key), "Incorrect key encoding");
      require(publicUri.getQuery() == null && publicUri.getFragment() == null, "Unescaped key");
      Files.write(local, bytes);
      MinioUtils.upload(bucket, "checks/local.txt", local.toFile());
      verifyDownload(service.getPresignedDownloadUrl("checks/local.txt"), bytes);
      UniStorageUtils.upload("checks/empty.txt", new byte[0], "txt");
      verifyDownload(service.getPresignedDownloadUrl("checks/empty.txt"), new byte[0]);
      require(client.headObject(b -> b.bucket(bucket).key(key)).contentType().startsWith("text/plain"), "Wrong content type");
      System.out.println("PASS: bytes, file, empty object, encoded keys, service URL overloads, record routing and signed downloads");
    } finally {
      if (created) {
        for (String object : new String[] {key, "checks/local.txt", "checks/empty.txt"}) {
          client.deleteObject(b -> b.bucket(bucket).key(object));
        }
        client.deleteBucket(b -> b.bucket(bucket));
      }
      Files.deleteIfExists(local);
      MinioUtils.shutdown();
    }
  }

  private static void verifyDownload(String url, byte[] expected) throws Exception {
    try (HttpClient http = HttpClient.newHttpClient()) {
      HttpResponse<byte[]> response = http.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
          HttpResponse.BodyHandlers.ofByteArray());
      require(response.statusCode() == 200, "Download status " + response.statusCode());
      require(Arrays.equals(expected, response.body()), "Downloaded bytes differ");
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}