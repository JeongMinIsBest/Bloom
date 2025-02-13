package com.rootimpact.transaction.dto.response;

import java.util.List;

public record TransactionFilterResponse(
        long currentPrice,
        TransactionSummaryResponse summary,
        List<TransactionDetailResponse> transactions
) {}
