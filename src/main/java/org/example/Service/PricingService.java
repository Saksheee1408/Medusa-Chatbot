package org.example.Service;

import org.example.Model.Price;
import org.example.Model.PriceList;
import org.example.Repository.PriceListRepository;
import org.example.Repository.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PricingService {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private PriceListRepository priceListRepository;

    // Price operations
    public List<Price> getProductPrices(String productId) {
        return priceRepository.findActiveProductPrices(productId);
    }

    public List<Price> getVariantPrices(String variantId) {
        return priceRepository.findActiveVariantPrices(variantId);
    }

    public List<Price> getCurrentPrices(String productId, String variantId) {
        return priceRepository.findCurrentPricesForItem(productId, variantId);
    }

    public List<Price> getPricesInRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return priceRepository.findPricesInRange(minPrice, maxPrice);
    }

    // Price List operations
    public List<PriceList> getActivePriceLists() {
        return priceListRepository.findActivePriceLists(LocalDateTime.now());
    }

    public List<PriceList> getPriceListsByType(String type) {
        return priceListRepository.findActiveByType(type);
    }

    public Optional<PriceList> getPriceListById(String id) {
        return priceListRepository.findById(id);
    }

    // Helper methods
    public Optional<BigDecimal> getLowestPrice(String productId, String variantId) {
        List<Price> prices = getCurrentPrices(productId, variantId);
        return prices.stream()
                .map(Price::getAmount)
                .min(BigDecimal::compareTo);
    }

    public Optional<BigDecimal> getHighestPrice(String productId, String variantId) {
        List<Price> prices = getCurrentPrices(productId, variantId);
        return prices.stream()
                .map(Price::getAmount)
                .max(BigDecimal::compareTo);
    }
}
