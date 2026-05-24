package ru.persea.recommendationservice.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.persea.recommendationservice.dto.BrandDto;
import ru.persea.recommendationservice.dto.CategoryDto;
import ru.persea.recommendationservice.handler.dto.ProductsSyncDto;
import ru.persea.recommendationservice.service.ProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final JdbcTemplate jdbcTemplate;

    private static final double MICROS_TO_SECONDS = 1_000_000.0;

    @Override
    public void insertBrand(BrandDto dto) {
        jdbcTemplate.update(
            "INSERT INTO brands (id, name) VALUES (?, ?) " +
            "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name",
            dto.id(), dto.name()
        );
    }

    @Override
    public void updateBrand(BrandDto dto) {
        jdbcTemplate.update(
            "UPDATE brands SET name = ? WHERE id = ?",
            dto.name(), dto.id()
        );
    }

    @Override
    public void deleteBrand(BrandDto dto) {
        jdbcTemplate.update("DELETE FROM brands WHERE id = ?", dto.id());
    }

    @Override
    public void insertCategory(CategoryDto dto) {
        jdbcTemplate.update(
            "INSERT INTO categories (id, name, code) VALUES (?, ?, ?) " +
            "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, code = EXCLUDED.code",
            dto.id(), dto.name(), dto.code()
        );
    }

    @Override
    public void updateCategory(CategoryDto dto) {
        jdbcTemplate.update(
            "UPDATE categories SET name = ?, code = ? WHERE id = ?",
            dto.name(), dto.code(), dto.id()
        );
    }

    @Override
    public void deleteCategory(CategoryDto dto) {
        jdbcTemplate.update("DELETE FROM categories WHERE id = ?", dto.id());
    }

    @Override
    public void insertProduct(ProductsSyncDto dto) {
        jdbcTemplate.update(
            "INSERT INTO products (id, name, brand_id, category_id, rating, image_uri, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, to_timestamp(?)) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "name = EXCLUDED.name, brand_id = EXCLUDED.brand_id, " +
            "category_id = EXCLUDED.category_id, rating = EXCLUDED.rating, " +
            "image_uri = EXCLUDED.image_uri, updated_at = EXCLUDED.updated_at",
            dto.id(), dto.name(), dto.brandId(), dto.categoryId(),
            dto.rating(), dto.imageUri(),
            dto.updatedAt() / MICROS_TO_SECONDS
        );
    }

    @Override
    public void updateProduct(ProductsSyncDto dto) {
        jdbcTemplate.update(
            "UPDATE products SET name = ?, brand_id = ?, category_id = ?, rating = ?, " +
            "image_uri = ?, updated_at = to_timestamp(?) WHERE id = ?",
            dto.name(), dto.brandId(), dto.categoryId(), dto.rating(), dto.imageUri(), dto.updatedAt(), dto.id()
        );
    }

    @Override
    public void deleteProduct(ProductsSyncDto dto) {
        jdbcTemplate.update("DELETE FROM products WHERE id = ?", dto.id());
    }
}