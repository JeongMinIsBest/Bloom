package com.rootimpact.transaction.controller;

import com.rootimpact.transaction.dto.request.TransactionCreateRequest;
import com.rootimpact.transaction.dto.response.*;
import com.rootimpact.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Tag(name = "거래 내역 API", description = "주식 거래 내역 관련 API")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "단일 거래 내역 조회",
            description = "거래 내역 ID에 해당하는 단일 거래 정보를 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 거래 내역을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDetailResponse> getTransaction(
            @Parameter(name = "id", description = "조회할 거래 내역의 ID", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        TransactionDetailResponse response = transactionService.getTransaction(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "사용자별 거래 내역 조회",
            description = "사용자 ID에 해당하는 모든 거래 정보를 조회."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/list")
    public ResponseEntity<List<TransactionDetailResponse>> getAllTransactions(
            @Parameter(name = "userId", description = "조회할 사용자의 ID", required = true, example = "10")
            @PathVariable("userId") Long userId
    ) {
        List<TransactionDetailResponse> responses = transactionService.getAllTransactions(userId);
        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "거래 내역 생성",
            description = "새로운 거래 내역을 생성합니다. (type: 매수, 매도 / 날짜 형식: 2025-02-13)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping
    public ResponseEntity<TransactionCreateResponse> createTransaction(
            @Parameter(description = "생성할 거래 내역 정보", required = true)
            @RequestBody TransactionCreateRequest request
    ) {
        TransactionCreateResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "실시간 가격정보 스트림",
            description = "외부 AI 모델을 통해 실시간 가격 및 gap 정보를 SSE로 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실시간 데이터 스트림 전송 성공"),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    @GetMapping(value = "/{userId}/price", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPriceSummary(
            @Parameter(name = "userId", description = "조회할 사용자의 ID", required = true, example = "10")
            @PathVariable("userId") Long userId,
            @Parameter(name = "companyName", description = "조회할 회사명", required = true, example = "ExampleCompany")
            @RequestParam("companyName") String companyName
    ) {
        // 타임아웃: 10분(600,000ms)
        SseEmitter emitter = new SseEmitter(600000L);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                TransactionFilterResponse response = transactionService.filterTransactions(userId, companyName);
                emitter.send(response);
            } catch (Exception e) {
                emitter.completeWithError(e);
                scheduler.shutdown();
            }
        }, 0, 2, TimeUnit.SECONDS);
        emitter.onCompletion(scheduler::shutdown);
        emitter.onTimeout(scheduler::shutdown);
        return emitter;
    }

    /**
     * 정적인 거래 내역 리스트 조회 엔드포인트
     */
    @Operation(
            summary = "거래 내역 리스트 조회",
            description = "DB에 저장된 거래 내역(회사명 필터 적용)을 최신 순으로 정렬하여 반환합니다."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/filter")
    public ResponseEntity<TransactionFilterListResponse> getTransactionFilterList(
            @Parameter(name = "userId", description = "조회할 사용자의 ID", required = true, example = "10")
            @PathVariable("userId") Long userId,
            @Parameter(name = "companyName", description = "조회할 회사명", required = true, example = "ExampleCompany")
            @RequestParam("companyName") String companyName
    ) {
        TransactionFilterListResponse response = transactionService.getTransactionFilterList(userId, companyName);
        return ResponseEntity.ok(response);
    }
}
