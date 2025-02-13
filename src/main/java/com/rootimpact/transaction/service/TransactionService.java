package com.rootimpact.transaction.service;

import com.rootimpact.stock.service.StockService;
import com.rootimpact.transaction.dto.request.TransactionCreateRequest;
import com.rootimpact.transaction.dto.response.TransactionCreateResponse;
import com.rootimpact.transaction.dto.response.TransactionDetailResponse;
import com.rootimpact.transaction.dto.response.TransactionFilterResponse;
import com.rootimpact.transaction.dto.response.TransactionSummaryResponse;
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
    // StockService 주입: 실시간 현재가 조회에 사용
    private final StockService stockService;

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

    /**
     * 사용자 ID와 회사명으로 거래 내역을 최신순(날짜 내림차순)으로 필터링하고
     * 해당 거래 내역을 기반으로 요약 정보를 계산하여 반환합니다.
     *
     * *실시간 현재가는 StockService.getStockPredictionByCompanyName()를 통해 조회합니다.
     */
    public TransactionFilterResponse filterTransactions(Long userId, String companyName) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();
        // 거래 내역 필터링 (최신 순)
        List<Transaction> transactions = transactionRepository.findByUserEntityAndCompanyNameOrderByDateDesc(userEntity, companyName);

        List<TransactionDetailResponse> transactionResponses = transactions.stream()
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

        // 요약 정보 계산 (가정: "매수", "매도" 문자열로 구분)
        long totalBuyQuantity = transactions.stream()
                .filter(t -> "매수".equals(t.getType()))
                .mapToLong(Transaction::getQuantity)
                .sum();

        long totalSellQuantity = transactions.stream()
                .filter(t -> "매도".equals(t.getType()))
                .mapToLong(Transaction::getQuantity)
                .sum();

        long holdingQuantity = totalBuyQuantity - totalSellQuantity;

        // "매수" 거래의 총 비용 (거래당 금액 * 수량)
        long totalBuyCost = transactions.stream()
                .filter(t -> "매수".equals(t.getType()))
                .mapToLong(t -> t.getAmount() * t.getQuantity())
                .sum();

        // 평균 매입 단가 (전체 매수 비용 / 전체 매수 수량)
        double averageBuyPrice = totalBuyQuantity > 0 ? (double) totalBuyCost / totalBuyQuantity : 0.0;
        long purchaseAmount = (long) (holdingQuantity * averageBuyPrice);

        // 실시간 현재가는 StockService에서 해당 회사의 StockPrediction으로 조회
        StockService.StockPrediction prediction = stockService.getStockPredictionByCompanyName(companyName);
        long currentPrice = prediction != null ? prediction.realTimeVariation()
                : (transactions.isEmpty() ? 0L : transactions.get(0).getAmount());
        long evaluationAmount = holdingQuantity * currentPrice;

        double rawRateOfReturn = purchaseAmount != 0 ? ((double) (evaluationAmount - purchaseAmount) / purchaseAmount) * 100 : 0.0;
        double rateOfReturn = Math.round(rawRateOfReturn * 100.0) / 100.0;

        TransactionSummaryResponse summaryResponse = new TransactionSummaryResponse(
                rateOfReturn,
                holdingQuantity,
                evaluationAmount,
                purchaseAmount
        );

        return new TransactionFilterResponse(currentPrice, summaryResponse, transactionResponses);
    }
}
