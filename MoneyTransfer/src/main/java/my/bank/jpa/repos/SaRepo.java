package my.bank.jpa.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import my.bank.entities.Account;
import my.bank.entities.ServiceAgreement;

@Repository
public interface SaRepo extends CrudRepository<ServiceAgreement, Long> {

	@Query(value = "SELECT sa FROM ServiceAgreement sa JOIN FETCH sa.transactions where sa.saId = :id")
    Optional<ServiceAgreement> findById(@Param("id") Long id);


	@Query(value = "SELECT sa FROM ServiceAgreement sa JOIN FETCH sa.transactions")
    Iterable<ServiceAgreement> findAll();
}
