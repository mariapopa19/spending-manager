package ro.mariapopa.spendingmanager.person;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/persons")
@Tag(name = "Person")
public class PersonController {
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public List<PersonResponse> findAll() {
        return personService.findAll();
    }

    @GetMapping("/{id}")
    public PersonResponse findById(@PathVariable Long id) {
        return personService.findById(id);
    }

    @PostMapping
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonRequest request) {
        PersonResponse created = personService.create(request);
        return ResponseEntity
                .created(URI.create("/api/persons/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public PersonResponse update(@PathVariable Long id, @Valid @RequestBody PersonRequest request) {
        return personService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        personService.delete(id);
    }
}
