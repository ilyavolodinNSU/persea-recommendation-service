package ru.persea.recommendationservice.repository;

import lombok.RequiredArgsConstructor;
import ru.persea.recommendationservice.dto.BrandDto;
import ru.persea.recommendationservice.dto.CategoryDto;
import ru.persea.recommendationservice.dto.ProductDto;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<ProductDto> productRowMapper = (rs, rowNum) ->
        new ProductDto(
            rs.getLong("id"),
            rs.getString("name"),
            new BrandDto(
                rs.getLong("brand_id"),
                rs.getString("brand_name")
            ),
            new CategoryDto(
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getString("category_code")
            ),
            rs.getInt("rating"),
            rs.getString("image_uri")
        );

    public List<ProductDto> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();

        String sql = """
            SELECT
                p.id,
                p.name,
                p.rating,
                p.image_uri,
                b.id   AS brand_id,
                b.name AS brand_name,
                c.id   AS category_id,
                c.name AS category_name,
                c.code AS category_code
            FROM products p
            JOIN brands     b ON b.id = p.brand_id
            JOIN categories c ON c.id = p.category_id
            WHERE p.id = ANY(?)
            ORDER BY ARRAY_POSITION(?, p.id)
            """;

        var idsArray = ids.toArray(Long[]::new);
        return jdbc.query(sql, productRowMapper, idsArray, idsArray);
    }

    public List<Long> findAllCategoryIds() {
        return jdbc.queryForList("SELECT id FROM categories", Long.class);
    }

    public List<ProductDto> findDefaultFeedProducts(int minRating, List<Long> categoryIds, int limit) {
        if (categoryIds.isEmpty()) return List.of();

        String sql = """
            SELECT
                p.id,
                p.name,
                p.rating,
                p.image_uri,
                b.id   AS brand_id,
                b.name AS brand_name,
                c.id   AS category_id,
                c.name AS category_name,
                c.code AS category_code
            FROM products p
            JOIN brands     b ON b.id = p.brand_id
            JOIN categories c ON c.id = p.category_id
            WHERE p.rating BETWEEN ? AND 100
              AND p.category_id = ANY(?)
            ORDER BY RANDOM()
            LIMIT ?
            """;

        Long[] catArray = categoryIds.toArray(Long[]::new);
        return jdbc.query(sql, productRowMapper, minRating, catArray, limit);
    }
}
