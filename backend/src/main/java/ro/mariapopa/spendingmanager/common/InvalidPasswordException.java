package ro.mariapopa.spendingmanager.common;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Old password is incorrect");
    }
}
