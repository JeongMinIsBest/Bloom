package com.rootimpact.transaction.dto.request;

public record TransactionCreateRequest(
        Long userId,
        String companyName,
        Long amount,
        Long quantity,
        String type,
        String date
) {}
