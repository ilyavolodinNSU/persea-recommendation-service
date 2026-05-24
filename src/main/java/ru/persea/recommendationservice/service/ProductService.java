package ru.persea.recommendationservice.service;

import ru.persea.recommendationservice.dto.BrandDto;
import ru.persea.recommendationservice.dto.CategoryDto;
import ru.persea.recommendationservice.handler.dto.ProductsSyncDto;

public interface ProductService {

    void insertBrand(BrandDto dto);

    void updateBrand(BrandDto dto);

    void deleteBrand(BrandDto dto);

    void insertCategory(CategoryDto dto);

    void updateCategory(CategoryDto dto);

    void deleteCategory(CategoryDto dto);

    void insertProduct(ProductsSyncDto dto);

    void updateProduct(ProductsSyncDto dto);

    void deleteProduct(ProductsSyncDto dto);
}