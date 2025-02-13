package com.rootimpact.transaction.dto.response;

public record TransactionPriceSummaryResponse(
        Long currentPrice,
        int gap
) {}
