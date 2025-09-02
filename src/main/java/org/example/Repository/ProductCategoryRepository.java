package org.example.Repository;

import org.example.Model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, String> {

    List<ProductCategory> findByNameContainingIgnoreCase(String name);

    List<ProductCategory> findByIsActiveTrue();

    // ✅ correct way: use parentCategory._id
    List<ProductCategory> findByParentCategory_Id(String parentId);

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.parentCategory IS NULL AND pc.isActive = true AND pc.deletedAt IS NULL")
    List<ProductCategory> findRootCategories();

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.name = :name AND pc.isActive = true AND pc.deletedAt IS NULL")
    Optional<ProductCategory> findActiveByName(@Param("name") String name);

    List<ProductCategory> findByDeletedAtIsNull();
}
