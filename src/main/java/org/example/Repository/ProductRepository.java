package org.example.Repository;

import org.example.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface  ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT p FROM Product p WHERE p.status = 'published'")
    List<Product> findPublishedProducts();
}
