package nextstep.subway.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {
    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        // 지하철_노선_생성_요청
        Map<String, String> params = new HashMap<>();
        params.put("name", "2호선");
        params.put("color", "green");

        ExtractableResponse<Response> response = 지하철_노선_생성_요청(params);

        //then
        // 지하철_노선_생성됨
        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

    }

    private ExtractableResponse<Response> 지하철_노선_생성_요청(Map<String, String> params) {
        return RestAssured
                    .given().log().all()
                    .body(params)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when().post("/lines")
                    .then().log().all().extract();
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {
        // given
        // 지하철_노선_등록되어_있음
        Map<String, String> params = new HashMap<>();
        params.put("name", "2호선");
        params.put("color", "green");
        지하철_노선_생성_요청(params);

        //when
        // 지하철_노선_생성_요청
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(params);

        //then
        // 지하철_노선_생성_실패됨
        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        // 지하철_노선_등록되어_있음
        // 지하철_노선_등록되어_있음
        Map<String, String> params = new HashMap<>();
        params.put("name", "2호선");
        params.put("color", "green");
        지하철_노선_생성_요청(params);

        params.put("name", "4호선");
        params.put("color", "blue");
        지하철_노선_생성_요청(params);


        //when
        // 지하철_노선_목록_조회_요청
        ExtractableResponse<Response> response = 지하철_노선_목록_조회_요청();

        //then
        // 지하철_노선_목록_응답됨
        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        // 지하철_노선_목록_포함됨
        List<String> lineNames = response.jsonPath().getList(".", LineResponse.class).stream()
                .map(LineResponse::getName)
                .collect(Collectors.toList());

        Assertions.assertThat(lineNames).contains("2호선","4호선");

    }

    private ExtractableResponse<Response> 지하철_노선_목록_조회_요청() {
        return RestAssured
                .given().log().all()
                .when().get("/lines")
                .then().log().all().extract();
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getLine() {
        // given
        // 지하철_노선_등록되어_있음
        Map<String, String> params = new HashMap<>();
        params.put("name", "4호선");
        params.put("color", "blue");
        ExtractableResponse<Response> request = 지하철_노선_생성_요청(params);


        //when
        // 지하철_노선_조회_요청
        LineResponse lineResponse = request.jsonPath().getObject(".", LineResponse.class);

        ExtractableResponse<Response> response = 지하철_노선_조회_요청(lineResponse.getId());

        //then
        // 지하철_노선_응답됨
        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private ExtractableResponse<Response> 지하철_노선_조회_요청(Long lineId) {
        return RestAssured
                .given().log().all()
                .when().get("/lines/" +lineId)
                .then().log().all().extract();
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        // 지하철_노선_등록되어_있음
        Map<String, String> params = new HashMap<>();
        params.put("name", "4호선");
        params.put("color", "blue");
        ExtractableResponse<Response> request = 지하철_노선_생성_요청(params);

        // when
        // 지하철_노선_수정_요청
        LineResponse lineResponse = request.jsonPath().getObject(".", LineResponse.class);
        params.put("name", "신4호선");
        params.put("color", "blue-green");



        ExtractableResponse<Response> response = 지하철_노선_수정_요청(params, lineResponse.getId());

        //then
        LineResponse finalResponse = response.jsonPath().getObject(".", LineResponse.class);
        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        Assertions.assertThat(finalResponse.getName()).isEqualTo("신4호선");
        Assertions.assertThat(finalResponse.getColor()).isEqualTo("blue-green");
    }

    private ExtractableResponse<Response> 지하철_노선_수정_요청(Map<String, String> params, Long lineId) {
        return RestAssured
                .given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put("/lines/"+lineId)
                .then().log().all().extract();
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        // 지하철_노선_등록되어_있음
        Map<String, String> params = new HashMap<>();
        params.put("name", "4호선");
        params.put("color", "blue");
        ExtractableResponse<Response> request = 지하철_노선_생성_요청(params);

        // when
        // 지하철_노선_제거_요청
        LineResponse lineResponse = request.jsonPath().getObject(".", LineResponse.class);
        //when

        ExtractableResponse<Response> response = 지하철_노선_삭제_요청(lineResponse.getId());

        //then
        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        // then
        // 지하철_노선_삭제됨
    }

    private ExtractableResponse<Response> 지하철_노선_삭제_요청(Long lineId) {
        return RestAssured
                .given().log().all()
                .when().delete("/lines/"+lineId)
                .then().log().all().extract();
    }
}
