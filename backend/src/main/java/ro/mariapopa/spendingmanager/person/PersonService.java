package ro.mariapopa.spendingmanager.person;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.mariapopa.spendingmanager.common.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional
public class PersonService {
    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public List<PersonResponse> findAll() {
        return personRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonResponse findById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person", id));
        return toResponse(person);
    }

    public PersonResponse create(PersonRequest personRequest) {
        Person person = new Person();
        person.setName(personRequest.name().trim());
        Person saved = personRepository.save(person);
        return toResponse(saved);
    }

    public PersonResponse update(Long id, PersonRequest personRequest) {
        Person person = personRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Person", id));
        person.setName(personRequest.name().trim());
        return toResponse(person);
    }

    public void delete(Long id) {
        if(!personRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person", id);
        }
        personRepository.deleteById(id);
    }

    private PersonResponse toResponse(Person person) {
        return new PersonResponse(
                person.getId(),
                person.getName()
        );
    }
}
