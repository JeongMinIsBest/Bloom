package com.rootimpact.transaction.service;

import com.rootimpact.transaction.dto.request.TransactionCreateRequest;
import com.rootimpact.transaction.dto.response.TransactionCreateResponse;
import com.rootimpact.transaction.dto.response.TransactionDetailResponse;
import com.rootimpact.transaction.entity.Transaction;
import com.rootimpact.transaction.repository.TransactionRepository;
import com.rootimpact.user.entity.UserEntity;
import com.rootimpact.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionDetailResponse getTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow();
        return new TransactionDetailResponse(
                transaction.getId(),
                transaction.getUserEntity().getId(),
                transaction.getCompanyName(),
                transaction.getAmount(),
                transaction.getQuantity(),
                transaction.getType(),
                transaction.getDate().toString()
        );
    }

    public List<TransactionDetailResponse> getAllTransactions(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();
        List<Transaction> transactions = transactionRepository.findAllByUserEntity(userEntity);
        return transactions.stream()
                .map(transaction -> new TransactionDetailResponse(
                        transaction.getId(),
                        transaction.getUserEntity().getId(),
                        transaction.getCompanyName(),
                        transaction.getAmount(),
                        transaction.getQuantity(),
                        transaction.getType(),
                        transaction.getDate().toString()
                ))
                .toList();
    }

    public TransactionCreateResponse createTransaction(TransactionCreateRequest request) {
        Transaction transaction = new Transaction();
        UserEntity userEntity = userRepository.findById(request.userId()).orElseThrow();
        transaction.setUserEntity(userEntity);
        transaction.setCompanyName(request.companyName());
        transaction.setAmount(request.amount());
        transaction.setQuantity(request.quantity());
        transaction.setType(request.type());
        transaction.setDate(Date.valueOf(request.date()));

        transactionRepository.save(transaction);
        return new TransactionCreateResponse(transaction.getId());
    }

}
