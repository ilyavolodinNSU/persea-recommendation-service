package ru.persea.recommendationservice.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.persea.recommendationservice.dto.BrandDto;
import ru.persea.recommendationservice.dto.CategoryDto;
import ru.persea.recommendationservice.dto.ProductDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryTest {

    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private ProductRepository productRepository;

    // ---------- findAllByIds ----------
    @Test
    void findAllByIds_emptyList_shouldReturnEmptyList() {
        List<ProductDto> result = productRepository.findAllByIds(Collections.emptyList());
        assertThat(result).isEmpty();
        verifyNoInteractions(jdbc);
    }

    @Test
    void findAllByIds_nonEmpty_shouldCallQueryWithCorrectArguments() {
        List<Long> ids = List.of(3L, 1L);
        ProductDto mockProduct = mock(ProductDto.class);
        when(jdbc.query(anyString(), any(RowMapper.class), any(Long[].class), any(Long[].class)))
                .thenReturn(List.of(mockProduct));

        List<ProductDto> result = productRepository.findAllByIds(ids);

        assertThat(result).containsExactly(mockProduct);
        ArgumentCaptor<Long[]> idsCaptor = ArgumentCaptor.forClass(Long[].class);
        verify(jdbc).query(contains("WHERE p.id = ANY(?)"), any(RowMapper.class),
                idsCaptor.capture(), idsCaptor.capture());
        // Проверим, что оба массива одинаковые (idsArray передаётся дважды)
        List<Long[]> captured = idsCaptor.getAllValues();
        assertThat(captured.get(0)).containsExactly(3L, 1L);
        assertThat(captured.get(1)).containsExactly(3L, 1L);
    }

    // ---------- findAllCategoryIds ----------
    @Test
    void findAllCategoryIds_shouldReturnListOfLongs() {
        when(jdbc.queryForList("SELECT id FROM categories", Long.class))
                .thenReturn(List.of(10L, 20L));

        List<Long> result = productRepository.findAllCategoryIds();

        assertThat(result).containsExactly(10L, 20L);
    }

    // ---------- findDefaultFeedProducts ----------
    @Test
    void findDefaultFeedProducts_emptyCategories_shouldReturnEmptyList() {
        List<ProductDto> result = productRepository.findDefaultFeedProducts(70, Collections.emptyList(), 10);
        assertThat(result).isEmpty();
        verifyNoInteractions(jdbc);
    }

    @Test
    void findDefaultFeedProducts_nonEmpty_shouldCallQueryWithCorrectParams() {
        List<Long> categoryIds = List.of(5L, 6L);
        ProductDto mockProduct = mock(ProductDto.class);
        when(jdbc.query(anyString(), any(RowMapper.class), eq(70), any(Long[].class), eq(10)))
                .thenReturn(List.of(mockProduct));

        List<ProductDto> result = productRepository.findDefaultFeedProducts(70, categoryIds, 10);

        assertThat(result).containsExactly(mockProduct);
        verify(jdbc).query(contains("WHERE p.rating BETWEEN ? AND 100"), any(RowMapper.class),
                eq(70), argThat(arr -> ((Long[]) arr).length == 2), eq(10));
    }

}