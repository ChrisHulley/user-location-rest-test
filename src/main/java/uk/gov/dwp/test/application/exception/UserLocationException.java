package uk.gov.dwp.test.application.exception;

public class UserLocationException extends Exception {
  public UserLocationException(Exception e) {
    super(e);
  }

  public UserLocationException(String exception) {
    super(exception);
  }
}
