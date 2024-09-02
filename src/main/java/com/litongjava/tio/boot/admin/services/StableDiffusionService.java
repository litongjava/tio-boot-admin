package com.litongjava.tio.boot.admin.services;

import java.io.IOException;
import java.util.Map;

import com.jfinal.kit.Kv;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.table.model.TableInput;
import com.litongjava.table.model.TableResult;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.admin.client.StableDiffusionClient;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.utils.json.Json;
import com.litongjava.tio.utils.resp.RespVo;
import com.litongjava.tio.utils.thread.TioThreadUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
@Slf4j
public class StableDiffusionService {
  public RespVo generateSd3(UploadFile uploadFile, Map<String, Object> requestMap) {
    RespVo retval = null;
    // 发送请求
    Response response = Aop.get(StableDiffusionClient.class).generateSd3(uploadFile, requestMap);
    if (response.isSuccessful()) {
      try {
        String contentType = response.header("Content-Type");
        String xRequestId = response.header("x-request-id");
        if (xRequestId == null) {
          xRequestId = System.currentTimeMillis() + "";
        }
        assert contentType != null;
        String suffix = contentType.split("/")[1];
        String fileName = xRequestId + "." + suffix;
        assert response.body() != null;
        byte[] bytes = response.body().bytes();
        // 上传文件
        retval = Aop.get(GoogleStorageService.class).uploadImageBytes(bytes, fileName, suffix, contentType);
      } catch (IOException e) {
        e.printStackTrace();
        return RespVo.fail(e.getMessage());
      }
    } else {
      String string = null;
      try {
        assert response.body() != null;
        string = response.body().string();
        Object parse = Json.getJson().parseObject(string);
        RespVo fail = RespVo.fail("Fail");
        fail.setData(parse);
        return fail;
      } catch (IOException e) {
        e.printStackTrace();
        return RespVo.fail(e.getMessage());
      }

    }

    // 记录数据库 异步
    if (retval != null) {
      // 使用ExecutorService异步执行任务
      RespVo finalRetval = retval;
      TioThreadUtils.getFixedThreadPool().submit(() -> {
        try {
          // 上传文件
          Object kv = null;
          if (uploadFile != null) {
            kv = Aop.get(GoogleStorageService.class).uploadImageToGoogle(uploadFile).getData();
          }

          saveToDb(requestMap, kv, finalRetval.getData());
        } catch (Exception e) {
          log.error("异步任务执行异常", e);
        }
      });
    }
    return retval;
  }

  public void saveToDb(Map<String, Object> requestMap, Object srcImage, Object dstImage) {
    TableInput kv =TableInput.create().set(requestMap);
    if (srcImage != null) {
      Object[] srcImages = new Object[]{srcImage};
      kv.set("src_images", srcImages);
    }

    Object[] dstImages = new Object[]{dstImage};

    kv.set("dst_images", dstImages);

    String[] jsonFields = {"src_images", "dst_images"};
    TableResult<Kv> save =ApiTable.save(TableNames.tio_boot_admin_sd_generated_history, kv,
      jsonFields);
    // DbJsonBean<Kv> save = DbJsonService.getInstance().save(TableNames.tio_boot_admin_sd_generated_history, kv);
    log.info("save result:{}", save);

  }
}
