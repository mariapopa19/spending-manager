package ro.mariapopa.spendingmanager.common;

public class DuplicateResourceException extends RuntimeException {
  public DuplicateResourceException(String resource, Object value) {
    super(resource + " already exist: " + value);
  }
}
