package ro.mariapopa.spendingmanager.common.entity;

import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

@MappedSuperclass
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_date")
public class SoftDeletedEntity extends BaseEntity {

}
