package org.example.Repository;

import org.example.Model.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, String> {

    List<PriceList> findByTitleContainingIgnoreCase(String title);

    List<PriceList> findByStatus(String status);

    @Query("SELECT pl FROM PriceList pl WHERE pl.status = 'active' AND " +
            "(pl.startsAt IS NULL OR pl.startsAt <= :now) AND " +
            "(pl.endsAt IS NULL OR pl.endsAt >= :now)")
    List<PriceList> findActivePriceLists(@Param("now") LocalDateTime now);

    @Query("SELECT pl FROM PriceList pl WHERE pl.type = :type AND pl.status = 'active'")
    List<PriceList> findActiveByType(@Param("type") String type);
}
