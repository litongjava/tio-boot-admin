package com.litongjava.tio.boot.admin.services;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson2.JSONArray;
import com.jfinal.kit.Kv;
import com.litongjava.table.model.TableInput;
import com.litongjava.table.model.TableResult;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.utils.resp.RespVo;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class PostsService {
  public RespVo save(TableInput kv) {
    JSONArray attachedImages = kv.getAs("attached_images");
    List<String> list = attachedImages.toJavaList(String.class);
    kv.set("created_at",new Date());

    String[] strings = list.toArray(new String[0]);
//    String[] strings = new String[list.size()];

    //get attached urls
//    for (int i = 0; i < list.size(); i++) {
//      String url = Aop.get(GoogleStorageService.class).getUrlByFileId(list.get(i));
//      strings[i] = url;
//    }


    kv.set("attached_images", strings);

    TableResult<Kv> dbJsonBean = ApiTable.saveOrUpdate(TableNames.posts, kv);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());

  }
}
