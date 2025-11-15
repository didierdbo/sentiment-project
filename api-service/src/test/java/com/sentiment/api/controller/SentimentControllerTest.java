package com.sentiment.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentiment.api.model.AnalysisRequest;
import com.sentiment.api.model.AnalysisResponse;
import com.sentiment.api.service.SentimentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for SentimentController
 * Tests all endpoints, validation rules, error handling, and CORS configuration
 */
@WebMvcTest(
    controllers = SentimentController.class,
    excludeAutoConfiguration = {
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        CacheAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    }
)
@ActiveProfiles("test")
@DisplayName("SentimentController Tests")
class SentimentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SentimentService sentimentService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private CacheManager cacheManager;

    @Nested
    @DisplayName("Health Endpoint Tests")
    class HealthEndpointTests {

        @Test
        @DisplayName("GET /api/v1/health should return 200 OK with status UP")
        void healthCheck_ShouldReturnOk() throws Exception {
            mockMvc.perform(get("/api/v1/health"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is("UP")));

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("GET /api/v1/health should have CORS headers")
        void healthCheck_ShouldHaveCorsHeaders() throws Exception {
            mockMvc.perform(get("/api/v1/health")
                            .header("Origin", "http://example.com"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Access-Control-Allow-Origin"));
        }
    }

    @Nested
    @DisplayName("Analyze Endpoint Tests")
    class AnalyzeEndpointTests {

        @Test
        @DisplayName("POST /api/v1/analyze with valid text should return 200 with analysis")
        void analyzeSentiment_WithValidText_ShouldReturnAnalysis() throws Exception {
            // Arrange
            String testText = "I love this product! It's amazing!";
            AnalysisRequest request = new AnalysisRequest(testText);
            AnalysisResponse mockResponse = new AnalysisResponse(
                    1L,
                    testText,
                    "POSITIVE",
                    0.95,
                    LocalDateTime.now()
            );

            when(sentimentService.analyze(testText)).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.text", is(testText)))
                    .andExpect(jsonPath("$.sentiment", is("POSITIVE")))
                    .andExpect(jsonPath("$.confidence", is(0.95)))
                    .andExpect(jsonPath("$.analyzedAt", notNullValue()));

            verify(sentimentService, times(1)).analyze(testText);
        }

        @Test
        @DisplayName("POST /api/v1/analyze with negative sentiment should return correct analysis")
        void analyzeSentiment_WithNegativeText_ShouldReturnNegativeAnalysis() throws Exception {
            // Arrange
            String testText = "This is terrible and disappointing.";
            AnalysisRequest request = new AnalysisRequest(testText);
            AnalysisResponse mockResponse = new AnalysisResponse(
                    2L,
                    testText,
                    "NEGATIVE",
                    0.88,
                    LocalDateTime.now()
            );

            when(sentimentService.analyze(testText)).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sentiment", is("NEGATIVE")))
                    .andExpect(jsonPath("$.confidence", is(0.88)));

            verify(sentimentService, times(1)).analyze(testText);
        }

        @Test
        @DisplayName("POST /api/v1/analyze with blank text should return 400 Bad Request")
        void analyzeSentiment_WithBlankText_ShouldReturnBadRequest() throws Exception {
            // Arrange
            AnalysisRequest request = new AnalysisRequest("   ");

            // Act & Assert
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.text", containsString("cannot be empty")));

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("POST /api/v1/analyze with null text should return 400 Bad Request")
        void analyzeSentiment_WithNullText_ShouldReturnBadRequest() throws Exception {
            // Arrange
            AnalysisRequest request = new AnalysisRequest(null);

            // Act & Assert
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("POST /api/v1/analyze with empty string should return 400 Bad Request")
        void analyzeSentiment_WithEmptyString_ShouldReturnBadRequest() throws Exception {
            // Arrange
            AnalysisRequest request = new AnalysisRequest("");

            // Act & Assert
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.text", containsString("cannot be empty")));

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("POST /api/v1/analyze with text exceeding 1000 chars should return 400")
        void analyzeSentiment_WithTextTooLong_ShouldReturnBadRequest() throws Exception {
            // Arrange - Create a string with 1001 characters
            String longText = "a".repeat(1001);
            AnalysisRequest request = new AnalysisRequest(longText);

            // Act & Assert
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.text", containsString("less than 1000 characters")));

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("POST /api/v1/analyze with exactly 1000 chars should succeed")
        void analyzeSentiment_WithExactly1000Chars_ShouldSucceed() throws Exception {
            // Arrange - Create a string with exactly 1000 characters
            String maxText = "a".repeat(1000);
            AnalysisRequest request = new AnalysisRequest(maxText);
            AnalysisResponse mockResponse = new AnalysisResponse(
                    3L,
                    maxText,
                    "NEUTRAL",
                    0.50,
                    LocalDateTime.now()
            );

            when(sentimentService.analyze(maxText)).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.text", is(maxText)));

            verify(sentimentService, times(1)).analyze(maxText);
        }

        @Test
        @DisplayName("POST /api/v1/analyze with missing request body should return 400")
        void analyzeSentiment_WithMissingBody_ShouldReturnBadRequest() throws Exception {
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("POST /api/v1/analyze with invalid JSON should return 400")
        void analyzeSentiment_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("POST /api/v1/analyze should have CORS headers")
        void analyzeSentiment_ShouldHaveCorsHeaders() throws Exception {
            String testText = "Test text";
            AnalysisRequest request = new AnalysisRequest(testText);
            AnalysisResponse mockResponse = new AnalysisResponse(
                    1L, testText, "POSITIVE", 0.9, LocalDateTime.now()
            );

            when(sentimentService.analyze(testText)).thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/analyze")
                            .header("Origin", "http://example.com")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Access-Control-Allow-Origin"));
        }

        @Test
        @DisplayName("POST /api/v1/analyze with special characters should succeed")
        void analyzeSentiment_WithSpecialCharacters_ShouldSucceed() throws Exception {
            String testText = "Hello! This is great 😊 #amazing @user";
            AnalysisRequest request = new AnalysisRequest(testText);
            AnalysisResponse mockResponse = new AnalysisResponse(
                    4L, testText, "POSITIVE", 0.92, LocalDateTime.now()
            );

            when(sentimentService.analyze(testText)).thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.text", is(testText)));

            verify(sentimentService, times(1)).analyze(testText);
        }
    }

    @Nested
    @DisplayName("History Endpoint Tests")
    class HistoryEndpointTests {

        @Test
        @DisplayName("GET /api/v1/history should return empty list when no history")
        void getHistory_WhenEmpty_ShouldReturnEmptyList() throws Exception {
            // Arrange
            when(sentimentService.getHistory()).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/v1/history"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(sentimentService, times(1)).getHistory();
        }

        @Test
        @DisplayName("GET /api/v1/history should return list of analyses")
        void getHistory_WithResults_ShouldReturnList() throws Exception {
            // Arrange
            List<AnalysisResponse> mockHistory = Arrays.asList(
                    new AnalysisResponse(1L, "Text 1", "POSITIVE", 0.9, LocalDateTime.now()),
                    new AnalysisResponse(2L, "Text 2", "NEGATIVE", 0.85, LocalDateTime.now()),
                    new AnalysisResponse(3L, "Text 3", "POSITIVE", 0.75, LocalDateTime.now())
            );

            when(sentimentService.getHistory()).thenReturn(mockHistory);

            // Act & Assert
            mockMvc.perform(get("/api/v1/history"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].text", is("Text 1")))
                    .andExpect(jsonPath("$[0].sentiment", is("POSITIVE")))
                    .andExpect(jsonPath("$[0].confidence", is(0.9)))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].sentiment", is("NEGATIVE")))
                    .andExpect(jsonPath("$[2].id", is(3)));

            verify(sentimentService, times(1)).getHistory();
        }

        @Test
        @DisplayName("GET /api/v1/history should return maximum 10 entries")
        void getHistory_ShouldReturnMaximum10Entries() throws Exception {
            // Arrange - Create a list with exactly 10 entries
            List<AnalysisResponse> mockHistory = Arrays.asList(
                    new AnalysisResponse(1L, "Text 1", "POSITIVE", 0.9, LocalDateTime.now()),
                    new AnalysisResponse(2L, "Text 2", "NEGATIVE", 0.8, LocalDateTime.now()),
                    new AnalysisResponse(3L, "Text 3", "POSITIVE", 0.85, LocalDateTime.now()),
                    new AnalysisResponse(4L, "Text 4", "POSITIVE", 0.7, LocalDateTime.now()),
                    new AnalysisResponse(5L, "Text 5", "NEGATIVE", 0.75, LocalDateTime.now()),
                    new AnalysisResponse(6L, "Text 6", "POSITIVE", 0.88, LocalDateTime.now()),
                    new AnalysisResponse(7L, "Text 7", "POSITIVE", 0.92, LocalDateTime.now()),
                    new AnalysisResponse(8L, "Text 8", "NEGATIVE", 0.78, LocalDateTime.now()),
                    new AnalysisResponse(9L, "Text 9", "POSITIVE", 0.95, LocalDateTime.now()),
                    new AnalysisResponse(10L, "Text 10", "POSITIVE", 0.89, LocalDateTime.now())
            );

            when(sentimentService.getHistory()).thenReturn(mockHistory);

            // Act & Assert
            mockMvc.perform(get("/api/v1/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(10)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[9].id", is(10)));

            verify(sentimentService, times(1)).getHistory();
        }

        @Test
        @DisplayName("GET /api/v1/history should have CORS headers")
        void getHistory_ShouldHaveCorsHeaders() throws Exception {
            when(sentimentService.getHistory()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/history")
                            .header("Origin", "http://example.com"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Access-Control-Allow-Origin"));
        }
    }

    @Nested
    @DisplayName("Test Cache Endpoint Tests")
    class TestCacheEndpointTests {

        @Test
        @DisplayName("GET /api/v1/test-cache with text parameter should return cache result")
        void testCache_WithText_ShouldReturnResult() throws Exception {
            // Arrange
            String testText = "cache test";
            String mockResult = "Cache test result: POSITIVE";

            when(sentimentService.testCache(testText)).thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/test-cache")
                            .param("text", testText))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(mockResult));

            verify(sentimentService, times(1)).testCache(testText);
        }

        @Test
        @DisplayName("GET /api/v1/test-cache without text parameter should return 400")
        void testCache_WithoutTextParam_ShouldReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/test-cache"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("GET /api/v1/test-cache with empty text should call service")
        void testCache_WithEmptyText_ShouldCallService() throws Exception {
            String emptyText = "";
            String mockResult = "Empty cache result";

            when(sentimentService.testCache(emptyText)).thenReturn(mockResult);

            mockMvc.perform(get("/api/v1/test-cache")
                            .param("text", emptyText))
                    .andExpect(status().isOk())
                    .andExpect(content().string(mockResult));

            verify(sentimentService, times(1)).testCache(emptyText);
        }

        @Test
        @DisplayName("GET /api/v1/test-cache should have CORS headers")
        void testCache_ShouldHaveCorsHeaders() throws Exception {
            String testText = "test";
            when(sentimentService.testCache(testText)).thenReturn("result");

            mockMvc.perform(get("/api/v1/test-cache")
                            .param("text", testText)
                            .header("Origin", "http://example.com"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Access-Control-Allow-Origin"));
        }
    }

    @Nested
    @DisplayName("CORS Configuration Tests")
    class CorsConfigurationTests {

        @Test
        @DisplayName("OPTIONS preflight request should be allowed")
        void preflightRequest_ShouldBeAllowed() throws Exception {
            mockMvc.perform(options("/api/v1/analyze")
                            .header("Origin", "http://example.com")
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "Content-Type"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Access-Control-Allow-Origin"))
                    .andExpect(header().exists("Access-Control-Allow-Methods"));
        }

        @Test
        @DisplayName("All endpoints should allow CORS from any origin")
        void allEndpoints_ShouldAllowCors() throws Exception {
            String origin = "http://different-domain.com";

            // Test health endpoint
            mockMvc.perform(get("/api/v1/health")
                            .header("Origin", origin))
                    .andExpect(header().string("Access-Control-Allow-Origin", "*"));

            // Test history endpoint
            when(sentimentService.getHistory()).thenReturn(Collections.emptyList());
            mockMvc.perform(get("/api/v1/history")
                            .header("Origin", origin))
                    .andExpect(header().string("Access-Control-Allow-Origin", "*"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("POST /api/v1/analyze with unsupported media type should return 415")
        void analyzeSentiment_WithUnsupportedMediaType_ShouldReturn415() throws Exception {
            mockMvc.perform(post("/api/v1/analyze")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("plain text"))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("GET request to /api/v1/analyze should return 405 Method Not Allowed")
        void analyzeSentiment_WithGetMethod_ShouldReturn405() throws Exception {
            mockMvc.perform(get("/api/v1/analyze"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());

            verifyNoInteractions(sentimentService);
        }

        @Test
        @DisplayName("POST request to /api/v1/health should return 405 Method Not Allowed")
        void health_WithPostMethod_ShouldReturn405() throws Exception {
            mockMvc.perform(post("/api/v1/health"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("Request to non-existent endpoint should return 404")
        void nonExistentEndpoint_ShouldReturn404() throws Exception {
            mockMvc.perform(get("/api/v1/nonexistent"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
