package com.rootimpact.stock.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
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
     * stock, real_time_variation 값만 추출하여 리스트로 반환
     */
    public List<StockPrediction> getStockPredictions() {
        // 외부 AI 모델 서버 URL (실제 서비스 URL에 맞게 수정)
        String url = "http://13.125.19.104:8000/predict";
        ResponseEntity<AiResponse> response = restTemplate.getForEntity(url, AiResponse.class);
        AiResponse aiResponse = response.getBody();

        List<StockPrediction> predictionsList = new ArrayList<>();
        if (aiResponse != null && aiResponse.getPredictions() != null) {
            for (Prediction prediction : aiResponse.getPredictions()) {
                predictionsList.add(new StockPrediction(
                        prediction.getStock(),
                        prediction.getReal_time_variation(),
                        prediction.getReal_time_variation() - prediction.getPredicted_close()));
            }
        }
        return predictionsList;
    }

    /**
     * 프론트엔드에 전달할 DTO: 주식명과 실시간 변동량만 포함
     */
    @Data
    public static class StockPrediction {
        private final String stock;
        private final int realTimeVariation;
        private final int gap;
    }

    /**
     * 외부 AI 서버의 응답 구조에 맞춘 DTO
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiResponse {
        private String date;
        private Weather weather;
        private List<Prediction> predictions;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        private double temperature;
        private int rainfall;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Prediction {
        private String stock;
        private int predicted_close;
        private int real_time_variation;
    }
}
