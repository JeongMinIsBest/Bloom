package com.rootimpact.transaction.service;

import com.rootimpact.stock.service.StockService;
import com.rootimpact.transaction.dto.request.TransactionCreateRequest;
import com.rootimpact.transaction.dto.response.*;
import com.rootimpact.transaction.entity.Transaction;
import com.rootimpact.transaction.repository.TransactionRepository;
import com.rootimpact.user.entity.UserEntity;
import com.rootimpact.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    // StockService 주입: 실시간 현재가 및 gap 조회에 사용
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
        UserEntity userEntity = userRepository.findById(1L).orElseThrow();
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
        UserEntity userEntity = userRepository.findById(1L).orElseThrow();
        int money = userEntity.getMoney();
        transaction.setUserEntity(userEntity);
        transaction.setCompanyName(request.companyName());
        transaction.setAmount(request.amount());
        transaction.setQuantity(request.quantity());
        transaction.setType(request.type());
        transaction.setDate(Date.valueOf(request.date()));

        if (request.type().equals("매수")) {
            userEntity.setMoney((int) (money - (request.amount() * request.quantity())));
        } else {
            userEntity.setMoney((int) (money + (request.amount() * request.quantity())));
        }

        userRepository.save(userEntity);
        transactionRepository.save(transaction);
        return new TransactionCreateResponse(transaction.getId());
    }

    /**
     * 실시간 가격정보를 StockService를 통해 조회하여 반환합니다.
     */
    public TransactionPriceSummaryResponse getPriceSummary(String companyName) {
        StockService.StockPrediction prediction = stockService.getStockPredictionByCompanyName(companyName);
        long currentPrice = prediction != null ? prediction.realTimeVariation() : 0L;
        int gap = prediction != null ? prediction.gap() : 0;
        return new TransactionPriceSummaryResponse(currentPrice, gap);
    }

    /**
     * 사용자와 회사명을 기준으로 거래 내역을 최신순으로 정렬하여 리스트로 반환합니다.
     */
    public TransactionFilterListResponse getTransactionFilterList(Long userId, String companyName) {
        UserEntity userEntity = userRepository.findById(1L).orElseThrow();
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
        return new TransactionFilterListResponse(transactionResponses);
    }

    /**
     * 사용자 ID와 회사명으로 거래 내역을 최신순(날짜 내림차순)으로 필터링하고,
     * 실시간 가격정보와 거래 요약 정보를 계산하여 TransactionFilterResponse를 반환합니다.
     */
    public TransactionFilterResponse filterTransactions(Long userId, String companyName) {
        UserEntity userEntity = userRepository.findById(1L).orElseThrow();
        List<Transaction> transactions = transactionRepository.findByUserEntityAndCompanyNameOrderByDateDesc(userEntity, companyName);

        // 정적 거래 내역 기반 계산
        long totalBuyQuantity = transactions.stream()
                .filter(t -> "매수".equals(t.getType()))
                .mapToLong(Transaction::getQuantity)
                .sum();
        long totalSellQuantity = transactions.stream()
                .filter(t -> "매도".equals(t.getType()))
                .mapToLong(Transaction::getQuantity)
                .sum();
        long holdingQuantity = totalBuyQuantity - totalSellQuantity;
        long totalBuyCost = transactions.stream()
                .filter(t -> "매수".equals(t.getType()))
                .mapToLong(t -> t.getAmount() * t.getQuantity())
                .sum();
        double averageBuyPrice = totalBuyQuantity > 0 ? (double) totalBuyCost / totalBuyQuantity : 0.0;
        long purchaseAmount = (long) (holdingQuantity * averageBuyPrice);

        // 실시간 현재가 및 gap 조회
        StockService.StockPrediction prediction = stockService.getStockPredictionByCompanyName(companyName);
        long currentPrice = prediction != null ? prediction.realTimeVariation()
                : (transactions.isEmpty() ? 0L : transactions.get(0).getAmount());
        int gap = prediction != null ? prediction.gap() : 0;
        long evaluationAmount = holdingQuantity * currentPrice;
        double rawRateOfReturn = purchaseAmount != 0 ? ((double) (evaluationAmount - purchaseAmount) / purchaseAmount) * 100 : 0.0;
        double rateOfReturn = Math.round(rawRateOfReturn * 100.0) / 100.0;

        TransactionPriceSummaryResponse priceSummary = new TransactionPriceSummaryResponse(currentPrice, gap);
        TransactionSummaryResponse summary = new TransactionSummaryResponse(currentPrice, gap, rateOfReturn, holdingQuantity, evaluationAmount, purchaseAmount);

        return new TransactionFilterResponse(priceSummary, summary);
    }

    /**
     * 사용자의 예수금 및 보유 주식 정보를 계산하여 PortfolioResponse를 반환합니다.
     * - 예수금: UserEntity.money
     * - 보유 주식 정보: 거래 내역을 회사별로 그룹화하여 보유 수량, 평가 금액, gap 계산
     */
    public PortfolioResponse getPortfolio(Long userId) {
        UserEntity userEntity = userRepository.findById(1L).orElseThrow();
        Long availableCash = userEntity.getMoney().longValue();

        // 사용자의 모든 거래 내역 조회
        List<Transaction> transactions = transactionRepository.findAllByUserEntity(userEntity);

        // 회사별로 그룹화
        Map<String, List<Transaction>> transactionsByCompany = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCompanyName));

        List<PositionResponse> positions = new ArrayList<>();
        long totalStockValue = 0;

        for (Map.Entry<String, List<Transaction>> entry : transactionsByCompany.entrySet()) {
            String companyName = entry.getKey();
            List<Transaction> companyTransactions = entry.getValue();

            // 보유 수량 계산: 매수 수량 - 매도 수량
            long totalBuy = companyTransactions.stream()
                    .filter(t -> "매수".equals(t.getType()))
                    .mapToLong(Transaction::getQuantity)
                    .sum();
            long totalSell = companyTransactions.stream()
                    .filter(t -> "매도".equals(t.getType()))
                    .mapToLong(Transaction::getQuantity)
                    .sum();
            long holdingQuantity = totalBuy - totalSell;
            if (holdingQuantity <= 0) {
                continue; // 보유 중이 아닌 회사는 제외
            }

            // 실시간 현재가 및 gap 조회
            StockService.StockPrediction prediction = stockService.getStockPredictionByCompanyName(companyName);
            long currentPrice = prediction != null ? prediction.realTimeVariation() : 0L;
            int gap = prediction != null ? prediction.gap() : 0;
            long evaluationAmount = holdingQuantity * currentPrice;
            totalStockValue += evaluationAmount;

            positions.add(new PositionResponse(companyName, holdingQuantity, evaluationAmount, gap));
        }

        return new PortfolioResponse(userEntity.getUserId(), availableCash, totalStockValue, positions);
    }

}
