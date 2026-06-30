package ro.mariapopa.spendingmanager.person;

import jakarta.persistence.*;
import ro.mariapopa.spendingmanager.common.entity.AuditableAndSoftDeletedEntity;

@Entity
@Table(name = "person")
public class Person extends AuditableAndSoftDeletedEntity {

    @Column(nullable = false)
    private String name;

    public Person() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                '}';
    }
}
