// CategoryService.java
package org.example.Service;

import org.example.Model.ProductCategory;
import org.example.Repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private ProductCategoryRepository categoryRepository;

    public List<ProductCategory> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public Optional<ProductCategory> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public List<ProductCategory> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<ProductCategory> getCategoryByName(String name) {
        return categoryRepository.findActiveByName(name);
    }

    public List<ProductCategory> getRootCategories() {
        return categoryRepository.findRootCategories();
    }
    public List<ProductCategory> getSubCategories(String parentId) {
        return categoryRepository.findByParentCategory_Id(parentId);
    }



}
