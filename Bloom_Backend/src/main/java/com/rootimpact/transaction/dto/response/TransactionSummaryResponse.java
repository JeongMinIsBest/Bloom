package com.rootimpact.transaction.dto.response;

public record TransactionSummaryResponse(
        long currentPrice,
        long gap,
        double rateOfReturn,      // 수익률 (%)
        long holdingQuantity,     // 현재 보유 수량
        long evaluationAmount,  // 평가 금액 (보유 수량 × 현재가)
        long purchaseAmount     // 매입 금액 (보유 수량에 해당하는 매수 비용)
) {}
