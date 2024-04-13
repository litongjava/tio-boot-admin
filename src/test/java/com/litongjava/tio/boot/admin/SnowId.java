package com.litongjava.tio.boot.admin;

import com.litongjava.data.utils.SnowflakeIdGenerator;
import org.junit.Test;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SnowId {

  @Test
  public void randomId() {
    long threadId = Thread.currentThread().getId();
    if (threadId > 31) {
      threadId = threadId % 31;
    }
    if (threadId < 0) {
      threadId = 0;
    }
    long id = new SnowflakeIdGenerator(threadId, 0).generateId();
    System.out.println(id);
  }
}
