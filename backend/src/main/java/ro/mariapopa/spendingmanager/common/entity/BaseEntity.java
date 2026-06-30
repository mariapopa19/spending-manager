package ro.mariapopa.spendingmanager.common.entity;

import jakarta.persistence.*;

@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version = 1;

    public Integer getVersion() {
        return version;
    }

    public Long getId() {
        return id;
    }
}