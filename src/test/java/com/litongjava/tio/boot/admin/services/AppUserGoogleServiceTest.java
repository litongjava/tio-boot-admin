package com.litongjava.tio.boot.admin.services;

import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.utils.url.UrlUtils;

public class AppUserGoogleServiceTest {

  @Test
  public void parseId() {
    Aop.get(AppUserGoogleService.class).parseGoogleId("{\r\n"
        + "  \"access_token\": \"ya29.a0AeXRPp7JJ5cTzccgFXXUZOSgTablJIcDyrmoKWdT3psZRZmStWM7PkTjzJgNqbocWft7E3DaTM4HiI4Cig0QJ5a4Ig7eoZt2kk2SqhqOt5rsVpQnsMrD9IWaN8-CYMV1kWDI-pRkFT5T3ndNuFbTSWgbEJb3JzOE_aR6OLNuvDwaCgYKAW8SARESFQHGX2MilqVn0sLMciCE5k7F2fLT5w0178\",\r\n"
        + "  \"expires_in\": 3114,\r\n"
        + "  \"scope\": \"https://www.googleapis.com/auth/userinfo.profile openid https://www.googleapis.com/auth/userinfo.email\",\r\n"
        + "  \"token_type\": \"Bearer\",\r\n"
        + "  \"id_token\": \"eyJhbGciOiJSUzI1NiIsImtpZCI6IjI1ZjgyMTE3MTM3ODhiNjE0NTQ3NGI1MDI5YjAxNDFiZDViM2RlOWMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIxMjUwNjI0NTY4NDMtYjBvNXNwNmNtMGZmZjA2N2xhbnBoNWk5dWYxbGloM2wuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIxMjUwNjI0NTY4NDMtYjBvNXNwNmNtMGZmZjA2N2xhbnBoNWk5dWYxbGloM2wuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDYyNjkzNzIzNTc2MTIxODY0OTkiLCJlbWFpbCI6ImxpdG9uZ2phdmEwMDFAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJUTG1XR0R3eWJpR2NEYmx3Ri1rUDRBIiwibmFtZSI6IlRvbmcgTGkiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jS3k1cXRYQ3JmdkdyTm95aEdZX0p2OGIzYUY5R2NCWHhvQU84NVJWTkxOZzg4ZHBDdz1zOTYtYyIsImdpdmVuX25hbWUiOiJUb25nIiwiZmFtaWx5X25hbWUiOiJMaSIsImlhdCI6MTc0MTUwODcyNSwiZXhwIjoxNzQxNTEyMzI1fQ.amUYFLpnE9ptysOtsD5rymStk_qvnACwrLeCq-an-2XXxSCR9n26exYtxSDava3if3YnNa-0z05XJA-cw5hsMEI4GNg47ffK_AuyyCPe_NxpEJ4190S9SgMm15EIvZfDIeTWWTh8TGF2bNR6JqD2nlLpL24q2JJoIjjo6GFUJGI6ZlQB5lHy71gO9rrhS3DTdKGMeDb0IP6fDNLdAkaFC2bCpN9XqO4ogYAaHshizm3cqgbPYBfKsqoRoDoLqraybzT78yoJ0-OFLcZlJ4yKpQvgZJS2AoGpK3BxXctsrs_3ed8TwNjD_giN_ogab3N4zj2OX-jdovjWfXPX0fu3-w\"\r\n"
        + "}");
  }
  @Test
  public void test() {
    
    //https://imaginix-eda2e.firebaseapp.com/__/auth/handler
    String url = "https://collegebot.ai/google/login";
//    String url="https://imaginix-eda2e.firebaseapp.com/__/auth/handler";
    String encode = UrlUtils.encode(url);
    System.out.println(encode);
    
    //String decode = UrlUtils.decode("https%3A%2F%2Frumi-bdb43.firebaseapp.com%2F__%2Fauth%2Fhandler");
    //System.out.println(decode);
  }

}
