package my.bank.jpa.repos;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import my.bank.entities.ServiceAgreement;
import my.bank.entities.Transaction;

public interface TransactionRepo extends CrudRepository<Transaction, String> {

	@Query(value = "SELECT tr FROM Transaction tr where tr.sa.saId = :id")
    List<Transaction> findBySaId(@Param("id") Long id);
}
