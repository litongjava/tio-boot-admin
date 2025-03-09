package com.litongjava.tio.boot.admin.services;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.http.response.ResponseVo;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.vo.GoogleJwtPayload;
import com.litongjava.tio.boot.admin.vo.GoogleToken;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.HttpUtils;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.json.JsonUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import okhttp3.FormBody;
import okhttp3.Request;

public class AppUserGoogleService {

  /**
   * 使用 Google 授权码进行登录
   */
  public RespBodyVo login(String code, String redirectUri) {
    // 从配置中读取 Google 的 Client ID 和 Client Secret
    String clientId = EnvUtils.get("GOOGLE_CLIENT_ID");
    String clientSecret = EnvUtils.get("GOOGLE_CLIENT_SECRET");

    // 构建请求 URL（Google 的 token 交换接口）
    String tokenUrl = "https://oauth2.googleapis.com/token";

    // 构建请求参数
    FormBody.Builder builder = new FormBody.Builder();
    builder.add("code", code).add("redirect_uri", redirectUri).add("grant_type", "authorization_code")
        //
        .add("client_id", clientId).add("client_secret", clientSecret);
    FormBody formBody = builder.build();

    Request request = new Request.Builder().url(tokenUrl).post(formBody).build();

    ResponseVo responseVo = HttpUtils.call(request);
    if (responseVo.isOk()) {
      // 根据 tokenResponse 解析出 access token 与用户信息（需要根据实际返回格式解析）
      String tokenResponse = responseVo.getBodyString();
      // 解析 tokenResponse，获得 googleId 并同步 google_info 信息
      GoogleJwtPayload googlePayload = parseGoogleId(tokenResponse);
      String googleId = googlePayload.getSub();

      // 检查数据库中是否存在该用户（这里建议根据 google_id 字段查找）
      Row row = Db.findFirst("SELECT id FROM app_users WHERE google_id = ?", googleId);

      String userId = null;
      String name = googlePayload.getName();
      String photo_url = googlePayload.getPicture();
      String email = googlePayload.getEmail();

      if (row == null) {
        Row emailRow = Db.findFirst("SELECT id,email FROM app_users WHERE email = ?", email);
        if (emailRow == null) {
          long longId = SnowflakeIdUtils.id();
          userId = String.valueOf(longId);
          Row user = Row.by("id", userId).set("email", email).set("display_name", name).set("photo_url", photo_url);
          Db.save(TioBootAdminTableNames.app_users, user);
        }
        userId = emailRow.getString("id");
      } else {
        userId = row.getString("id");
      }

      // 生成系统内部 token，有效期 7 天（604800秒）
      Long timeout = EnvUtils.getLong("app.token.timeout", 604800L);
      Long tokenTimeout = System.currentTimeMillis() / 1000 + timeout;
      AppUserService appUserService = Aop.get(AppUserService.class);
      String token = appUserService.createToken(userId, tokenTimeout);
      String refreshToken = appUserService.createRefreshToken(userId);

      Kv kv = Kv.by("user_id", userId).set("token", token).set("expires_in", tokenTimeout.intValue()).set("refresh_token", refreshToken)
          //
          .set("display_name", name).set("photo_url", photo_url);
      return RespBodyVo.ok(kv);
    } else {
      return RespBodyVo.fail(responseVo.getBodyString());
    }
  }

  /**
   * 根据 tokenResponse 解析出 Google 用户唯一标识（示例实现）
   */
  public GoogleJwtPayload parseGoogleId(String tokenResponse) {
    GoogleToken googleToken = FastJson2Utils.parse(tokenResponse, GoogleToken.class);
    String idToken = googleToken.getId_token();

    // 解码 JWT 的 payload 部分
    String[] tokenParts = idToken.split("\\.");
    if (tokenParts.length < 2) {
      throw new IllegalArgumentException("无效的 id_token");
    }
    // 注意：JWT 分为 header.payload.signature，此处只需要 payload
    byte[] decodedBytes = Base64.getUrlDecoder().decode(tokenParts[1]);
    String payloadJsonStr = new String(decodedBytes, StandardCharsets.UTF_8);
    return JsonUtils.parse(payloadJsonStr, GoogleJwtPayload.class);
  }
}