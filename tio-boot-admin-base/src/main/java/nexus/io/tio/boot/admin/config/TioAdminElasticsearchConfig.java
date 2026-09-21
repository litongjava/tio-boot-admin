package nexus.io.tio.boot.admin.config;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import lombok.extern.slf4j.Slf4j;
import nexus.io.es.client.Elastic;
import nexus.io.hook.HookCan;
import nexus.io.tio.utils.environment.EnvUtils;
import nexus.io.tio.utils.hutool.StrUtil;

@Slf4j
public class TioAdminElasticsearchConfig {

  public void config() {
    // 读取配置属性
    String urls = EnvUtils.get("elasticsearch.rest.urls");
    String username = EnvUtils.get("elasticsearch.rest.username");
    String password = EnvUtils.get("elasticsearch.rest.password");

    if (StrUtil.isEmpty(urls)) {
      return;
    }

    // 创建 HttpHost 实例
    HttpHost httpHost = HttpHost.create(urls);

    // 配置 RestClientBuilder
    RestClientBuilder builder = RestClient.builder(httpHost);

    if (StrUtil.isNotEmpty(username)) {
      // 设置凭证提供者
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

      // 配置 HTTP 客户端回调
      builder.setHttpClientConfigCallback(
          httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
    }

    // 创建 RestHighLevelClient
    RestHighLevelClient client = new RestHighLevelClient(builder);

    // 将客户端添加到 Elastic 管理类
    Elastic.setClient(client);

    // 确保在关闭时关闭客户端
    HookCan.me().addDestroyMethod(() -> {
      try {
        client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    // 测试连接状态
    try {
      log.info("client:{}", client.ping(RequestOptions.DEFAULT));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
