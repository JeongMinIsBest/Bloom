package com.rootimpact.transaction.dto.response;

public record TransactionFilterResponse(
        TransactionPriceSummaryResponse priceSummary,
        TransactionSummaryResponse summary
) {}
