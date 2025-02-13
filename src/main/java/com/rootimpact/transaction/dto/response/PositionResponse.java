package com.rootimpact.transaction.dto.response;

public record PositionResponse(
    String companyName,    // 주식회사 이름
    Long holdingQuantity,  // 보유 수량
    Long evaluationAmount, // 평가 금액 (보유 수량 × 현재가)
    int gap                // 기준가와의 차이
) {}
