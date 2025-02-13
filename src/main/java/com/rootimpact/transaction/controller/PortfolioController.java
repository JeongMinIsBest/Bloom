package com.rootimpact.transaction.controller;

import com.rootimpact.stock.service.StockService;
import com.rootimpact.transaction.dto.response.PortfolioResponse;
import com.rootimpact.transaction.dto.response.PositionResponse;
import com.rootimpact.transaction.entity.Transaction;
import com.rootimpact.transaction.repository.TransactionRepository;
import com.rootimpact.transaction.service.TransactionService;
import com.rootimpact.user.entity.UserEntity;
import com.rootimpact.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Tag(name = "포트폴리오 API", description = "사용자 예수금 및 보유 주식 가치 정보를 실시간으로 전송")
public class PortfolioController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    // StockService 주입: 실시간 현재가 및 gap 조회에 사용
    private final StockService stockService;
    private final TransactionRepository transactionRepository;

    @Operation(
            summary = "실시간 포트폴리오 정보 스트림",
            description = "사용자의 예수금 및 보유 주식 가치 총합, 그리고 각 주식의 회사 이름, 보유 수량, 평가 금액, 기준가와의 차이(gap)를 SSE로 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실시간 데이터 스트림 전송 성공"),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPortfolio(
            @RequestParam("userId") Long userId
    ) {
        // 타임아웃: 10분 (600,000ms)
        SseEmitter emitter = new SseEmitter(600000L);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                PortfolioResponse portfolio = transactionService.getPortfolio(userId);
                emitter.send(portfolio);
            } catch (Exception e) {
                emitter.completeWithError(e);
                scheduler.shutdown();
            }
        }, 0, 2, TimeUnit.SECONDS);
        emitter.onCompletion(scheduler::shutdown);
        emitter.onTimeout(scheduler::shutdown);
        return emitter;
    }

}
