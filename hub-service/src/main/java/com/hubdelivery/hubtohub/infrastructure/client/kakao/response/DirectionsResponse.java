package com.hubdelivery.hubtohub.infrastructure.client.kakao.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
public class DirectionsResponse {

    private List<Route> routes;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Setter
    public static class Route {

        @JsonProperty("result_code")
        private Integer resultCode;

        @JsonProperty("result_msg")
        private String resultMsg;


        private Summary summary;
        private List<Section> sections;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Setter
    public static class Summary {
        private Integer distance;
        private Integer duration;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Setter
    public static class Section {
        private Integer distance;       // 구간 거리 (m)
        private Integer duration;       // 구간 시간 (초)
        private Bound bound;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Setter
    public static class Bound {
        private Double minX;
        private Double minY;
        private Double maxX;
        private Double maxY;
    }
}
