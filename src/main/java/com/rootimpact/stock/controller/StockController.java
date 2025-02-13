package com.rootimpact.stock.controller;

import com.rootimpact.stock.service.StockService;
import com.rootimpact.stock.service.StockService.StockPrediction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
@Tag(name = "주식 API", description = "주식 예측 및 실시간 데이터 스트림 API")
public class StockController {

    private final StockService stockService;

    @Operation(
            summary = "실시간 주식 예측 데이터 스트림",
            description = "외부 AI 모델 서버의 /predict 엔드포인트를 호출하여 2초마다 주식 예측 데이터를 SSE를 통해 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실시간 데이터 스트림 전송 성공"),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getStock() {
        SseEmitter emitter = new SseEmitter();

        // ScheduledExecutorService를 사용하여 1초마다 작업 수행
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<StockPrediction> predictions = stockService.getStockPredictions();
                emitter.send(predictions);
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
