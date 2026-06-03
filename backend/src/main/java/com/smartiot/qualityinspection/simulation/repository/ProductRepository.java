package com.smartiot.qualityinspection.simulation.repository;

import com.smartiot.qualityinspection.simulation.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);
}
