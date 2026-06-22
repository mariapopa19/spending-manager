package ro.mariapopa.spendingmanager.person;

public class PersonNotFoundException extends RuntimeException {
    public PersonNotFoundException(Long id) {
        super("Person not found: " + id);
    }
}
