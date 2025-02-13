package com.rootimpact.transaction.repository;

import com.rootimpact.transaction.entity.Transaction;
import com.rootimpact.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUserEntity(UserEntity userEntity);
}
