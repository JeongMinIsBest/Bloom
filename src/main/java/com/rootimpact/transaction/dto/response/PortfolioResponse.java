package com.rootimpact.transaction.dto.response;

import java.util.List;

public record PortfolioResponse(
    String userName,
    Long availableCash,               // 사용자의 예수금
    Long totalStockValue,             // 보유 주식 평가 금액 총합
    List<PositionResponse> stocks  // 각 주식 포지션 목록
) {}
