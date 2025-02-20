package com.rootimpact.stock.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 외부 AI 서버의 /predict 엔드포인트를 호출해 JSON 응답에서 predictions 배열 내
     * stock, real_time_variation 값만 추출하여 리스트로 반환합니다.
     */
    public List<StockPrediction> getStockPredictions() {
        // 외부 AI 모델 서버 URL (실제 서비스 URL에 맞게 수정)
        String url = "http://13.125.19.104:8000/predict";
        ResponseEntity<AiResponse> response = restTemplate.getForEntity(url, AiResponse.class);
        AiResponse aiResponse = response.getBody();

        List<StockPrediction> predictionsList = new ArrayList<>();
        if (aiResponse != null && aiResponse.predictions() != null) {
            for (Prediction prediction : aiResponse.predictions()) {
                predictionsList.add(new StockPrediction(
                        prediction.stock(),
                        prediction.real_time_variation(),
                        prediction.real_time_variation() - prediction.predicted_close()));
            }
        }
        return predictionsList;
    }

    /**
     * 회사명으로 StockPrediction 객체를 조회합니다.
     */
    public StockPrediction getStockPredictionByCompanyName(String companyName) {
        return getStockPredictions().stream()
                .filter(sp -> sp.stock().equals(companyName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 프론트엔드에 전달할 DTO (record 사용): 회사명, 실시간 변동값, 기준가와의 차이
     */
    public record StockPrediction(String stock, int realTimeVariation, int gap) {}

    /**
     * 외부 AI 서버의 응답 구조에 맞춘 DTO (record 사용)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AiResponse(String date, Weather weather, List<Prediction> predictions) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weather(double temperature, int rainfall) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Prediction(String stock, int predicted_close, int real_time_variation) {}
}
