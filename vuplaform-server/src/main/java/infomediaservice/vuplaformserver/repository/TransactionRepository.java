package infomediaservice.vuplaformserver.repository;

import infomediaservice.vuplaformserver.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}
