package ru.persea.recommendationservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.persea.recommendationservice.batch.RecommendationJob;
import ru.persea.recommendationservice.dto.ProductDto;
import ru.persea.recommendationservice.service.FeedService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    @Mock
    private RecommendationJob job;

    @Mock
    private FeedService feedService;

    @InjectMocks
    private FeedController controller;

    @Test
    void recalculate_shouldReturnAccepted() {
        ResponseEntity<String> response = controller.recalculate();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo("recalculation started");
    }

    @Test
    void getUserFeed_shouldReturnFeed() {
        String keycloakId = "user1";
        int offset = 0;
        int limit = 20;
        List<ProductDto> expected = List.of(mock(ProductDto.class));
        when(feedService.getFeed(keycloakId, offset, limit)).thenReturn(expected);

        ResponseEntity<List<ProductDto>> response = controller.getUserFeed(keycloakId, offset, limit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void getMyFeed_shouldReturnFeedForCurrentUser() {
        int offset = 0;
        int limit = 20;
        String subject = "auth0|123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(subject);
        JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
        when(jwtToken.getToken()).thenReturn(jwt);

        List<ProductDto> expected = List.of(mock(ProductDto.class));
        when(feedService.getFeed(subject, offset, limit)).thenReturn(expected);

        ResponseEntity<List<ProductDto>> response = controller.getMyFeed(offset, limit, jwtToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(feedService).getFeed(subject, offset, limit);
    }
}