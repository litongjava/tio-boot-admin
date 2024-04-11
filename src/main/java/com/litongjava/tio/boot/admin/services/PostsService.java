package com.litongjava.tio.boot.admin.services;

import com.alibaba.fastjson2.JSONArray;
import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.utils.resp.RespVo;

import java.util.List;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class PostsService {
  public RespVo save(Kv kv) {
    JSONArray attachedImages = kv.getAs("attached_images");
    List<String> list = attachedImages.toJavaList(String.class);

    String[] strings = list.toArray(new String[0]);
//    String[] strings = new String[list.size()];

    //get attached urls
//    for (int i = 0; i < list.size(); i++) {
//      String url = Aop.get(GoogleStorageService.class).getUrlByFileId(list.get(i));
//      strings[i] = url;
//    }


    kv.set("attached_images", strings);

    DbJsonService dbJsonService = Aop.get(DbJsonService.class);
    //
    DbJsonBean<Kv> dbJsonBean = dbJsonService.saveOrUpdate(TableNames.posts, kv);

    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());

  }
}
