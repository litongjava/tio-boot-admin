package com.litongjava.tio.boot.admin.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.jfinal.kit.Kv;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.utils.PDDocumentUtils;
import com.litongjava.tio.utils.http.HttpDownloadUtils;

public class DocumentService {

  public Kv generateAndSavePDFthumbnail(String category, Kv inputParam) {
    String url = inputParam.getStr("url");

    ByteArrayOutputStream stream = HttpDownloadUtils.download(url, null);
    byte[] pdfBytes = stream.toByteArray();

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pdfBytes);
    ByteArrayOutputStream thumbnailStream = PDDocumentUtils.extraThumbnail(byteArrayInputStream);
    byte[] imageBytes = thumbnailStream.toByteArray();

    long currentTimeMillis = System.currentTimeMillis();

    String filename = currentTimeMillis + ".png";
    inputParam = Aop.get(AwsS3StorageService.class).uploadReturnKv(category, filename, imageBytes.length, imageBytes);

    Kv resultKv = Kv.create()
        //
        .set("id", inputParam.getStr("id")).set("uid", "rc-upload-" + System.currentTimeMillis() + "-2")
        //
        .set("url", inputParam.getStr("url")).set("name", filename)
        //
        .set("size", imageBytes.length).set("type", "image/png").set("status", "done");

    return resultKv;
  }

}
