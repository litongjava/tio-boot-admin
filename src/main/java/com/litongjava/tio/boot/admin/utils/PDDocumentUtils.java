package com.litongjava.tio.boot.admin.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PDDocumentUtils {

  public static ByteArrayOutputStream extraThumbnail(ByteArrayInputStream byteArrayInputStream) {
    try (PDDocument document = PDDocument.load(byteArrayInputStream)) {
      // 读取文件
      PDFRenderer renderer = new PDFRenderer(document);
      // 将第一页转为T恤
      BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 200);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "png", outputStream);
      return outputStream;
      // 你现在可以使用这个 Kv 对象进行进一步处理
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
