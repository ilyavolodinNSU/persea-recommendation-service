package ru.persea.recommendationservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.persea.recommendationservice.dto.BrandDto;
import ru.persea.recommendationservice.dto.CategoryDto;
import ru.persea.recommendationservice.handler.dto.ProductsSyncDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ProductServiceImpl productService;

    // ---------- Brand tests ----------
    @Test
    void insertBrand_shouldExecuteUpsert() {
        BrandDto dto = new BrandDto(1L, "Samsung");

        productService.insertBrand(dto);

        verify(jdbcTemplate).update(
                eq("INSERT INTO brands (id, name) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name"),
                eq(1L), eq("Samsung")
        );
    }

    @Test
    void updateBrand_shouldExecuteUpdate() {
        BrandDto dto = new BrandDto(2L, "Apple");

        productService.updateBrand(dto);

        verify(jdbcTemplate).update(
                eq("UPDATE brands SET name = ? WHERE id = ?"),
                eq("Apple"), eq(2L)
        );
    }

    @Test
    void deleteBrand_shouldExecuteDelete() {
        BrandDto dto = new BrandDto(3L, "Any");

        productService.deleteBrand(dto);

        verify(jdbcTemplate).update("DELETE FROM brands WHERE id = ?", 3L);
    }

    // ---------- Category tests ----------
    @Test
    void insertCategory_shouldExecuteUpsert() {
        CategoryDto dto = new CategoryDto(10L, "Электроника", "electronics");

        productService.insertCategory(dto);

        verify(jdbcTemplate).update(
                eq("INSERT INTO categories (id, name, code) VALUES (?, ?, ?) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, code = EXCLUDED.code"),
                eq(10L), eq("Электроника"), eq("electronics")
        );
    }

    @Test
    void updateCategory_shouldExecuteUpdate() {
        CategoryDto dto = new CategoryDto(10L, "Техника", "tech");

        productService.updateCategory(dto);

        verify(jdbcTemplate).update(
                eq("UPDATE categories SET name = ?, code = ? WHERE id = ?"),
                eq("Техника"), eq("tech"), eq(10L)
        );
    }

    @Test
    void deleteCategory_shouldExecuteDelete() {
        CategoryDto dto = new CategoryDto(20L, "Any", "code");

        productService.deleteCategory(dto);

        verify(jdbcTemplate).update("DELETE FROM categories WHERE id = ?", 20L);
    }

    // ---------- Product tests ----------
    @Test
    void insertProduct_shouldExecuteUpsertWithMicrosConversion() {
        ProductsSyncDto dto = new ProductsSyncDto(
                100L, "Товар", 1L, 2L, 5, "img.png", 1_700_000_000_000L
        );
        double expectedSeconds = 1_700_000.0; // micros / 1_000_000

        productService.insertProduct(dto);

        verify(jdbcTemplate).update(
                eq("INSERT INTO products (id, name, brand_id, category_id, rating, image_uri, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, to_timestamp(?)) " +
                        "ON CONFLICT (id) DO UPDATE SET " +
                        "name = EXCLUDED.name, brand_id = EXCLUDED.brand_id, " +
                        "category_id = EXCLUDED.category_id, rating = EXCLUDED.rating, " +
                        "image_uri = EXCLUDED.image_uri, updated_at = EXCLUDED.updated_at"),
                eq(100L), eq("Товар"), eq(1L), eq(2L), eq(5), eq("img.png"),
                eq(expectedSeconds)
        );
    }

    @Test
    void updateProduct_shouldExecuteUpdateWithRawUpdatedAt() {
        ProductsSyncDto dto = new ProductsSyncDto(
                200L, "Обновлённый", 3L, 4L, 4, "new.png", 1_700_000_000_000L
        );

        productService.updateProduct(dto);

        verify(jdbcTemplate).update(
                eq("UPDATE products SET name = ?, brand_id = ?, category_id = ?, rating = ?, " +
                        "image_uri = ?, updated_at = to_timestamp(?) WHERE id = ?"),
                eq("Обновлённый"), eq(3L), eq(4L), eq(4), eq("new.png"),
                eq(1_700_000_000_000L), eq(200L)   // без деления
        );
    }

    @Test
    void deleteProduct_shouldDeleteActionsAndProduct() {
        ProductsSyncDto dto = new ProductsSyncDto(300L, "Любой", 1L, 1L, 3, "img", 0L);

        productService.deleteProduct(dto);

        verify(jdbcTemplate).update("DELETE FROM user_actions WHERE product_id = ?", 300L);
        verify(jdbcTemplate).update("DELETE FROM products WHERE id = ?", 300L);
    }
}