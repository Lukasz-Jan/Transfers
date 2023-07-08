package my.bank.jpa.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import my.bank.entities.Account;

@Repository
public interface AcctRepo extends CrudRepository<Account, String> {	

	
	
    @Query(value = "SELECT acc FROM Account acc JOIN FETCH acc.agreements where acc.acctId = :id")
    Optional<Account> findById(@Param("id") String id);
	
    @Query(value = "SELECT acc FROM Account acc JOIN FETCH acc.agreements")
    Iterable<Account> findAll();
}
