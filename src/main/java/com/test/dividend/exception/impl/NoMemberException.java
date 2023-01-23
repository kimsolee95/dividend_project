package com.test.dividend.exception.impl;

import com.test.dividend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NoMemberException extends AbstractException {

  @Override
  public int getStatusCode() {
    return HttpStatus.BAD_REQUEST.value();
  }

  @Override
  public String getMessage() {
    return "존재하지 않는 ID입니다.";
  }
}
