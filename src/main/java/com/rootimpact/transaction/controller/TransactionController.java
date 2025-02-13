package com.rootimpact.transaction.controller;

import com.rootimpact.transaction.dto.request.TransactionCreateRequest;
import com.rootimpact.transaction.dto.response.TransactionCreateResponse;
import com.rootimpact.transaction.dto.response.TransactionDetailResponse;
import com.rootimpact.transaction.dto.response.TransactionFilterResponse;
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
            @Parameter(
                    name = "id",
                    description = "조회할 거래 내역의 ID",
                    required = true,
                    example = "1"
            )
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
            @Parameter(
                    name = "userId",
                    description = "조회할 사용자의 ID",
                    required = true,
                    example = "10"
            )
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
            @Parameter(
                    description = "생성할 거래 내역 정보",
                    required = true
            )
            @RequestBody TransactionCreateRequest request
    ) {
        TransactionCreateResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "실시간 거래 내역 필터링 스트림",
            description = "주어진 사용자 ID와 회사명에 해당하는 거래 내역을 최신 순으로 정렬하여 실시간으로 스트리밍하고, 거래 요약 정보를 함께 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실시간 데이터 스트림 전송 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없음")
    })
    @GetMapping(value = "/{userId}/filter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamFilterTransactions(
            @Parameter(
                    name = "userId",
                    description = "조회할 사용자의 ID",
                    required = true,
                    example = "10"
            )
            @PathVariable("userId") Long userId,
            @Parameter(
                    name = "companyName",
                    description = "조회할 회사명",
                    required = true,
                    example = "ExampleCompany"
            )
            @RequestParam("companyName") String companyName
    ) {
        SseEmitter emitter = new SseEmitter();

        // ScheduledExecutorService를 사용하여 2초마다 거래 내역과 요약 정보를 갱신하여 전송
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

        // 클라이언트가 연결 종료되거나 타임아웃될 때 스케줄러 종료
        emitter.onCompletion(scheduler::shutdown);
        emitter.onTimeout(scheduler::shutdown);

        return emitter;
    }
}
