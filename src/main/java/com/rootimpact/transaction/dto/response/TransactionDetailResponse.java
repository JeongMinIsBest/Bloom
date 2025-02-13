package com.rootimpact.transaction.dto.response;

public record TransactionDetailResponse(
        Long id,
        Long userId,
        String companyName,
        Long amount,
        Long quantity,
        String type,
        String date
) {}
