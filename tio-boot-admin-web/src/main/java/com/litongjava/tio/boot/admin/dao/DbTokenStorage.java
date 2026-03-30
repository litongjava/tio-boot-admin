package com.litongjava.tio.boot.admin.dao;

import com.litongjava.tio.utils.token.ITokenStorage;

public class DbTokenStorage implements ITokenStorage {

  @Override
  public void put(Object userId, String tokenValue) {

  }

  @Override
  public boolean containsKey(Object userId) {
    return false;
  }

  @Override
  public String remove(Object userId) {
    return null;
  }
}