package com.rootimpact.transaction.dto.response;

import java.util.List;

public record TransactionFilterListResponse(
        List<TransactionDetailResponse> transactions
) {
}
