package com.group1.catalogservice.domain;

import com.group1.catalogservice.domain.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

interface ProductRepository extends JpaRepository<ProductEntity,Long>, JpaSpecificationExecutor<ProductEntity> {

    Optional<ProductEntity> findByCodeAndStatusNot(String code, Status status);

    Page<ProductEntity> findByStatusNot(Status status, Pageable pageable);


    // Helper method for validation during creation
    boolean existsByCodeOrIsbn(String code, String isbn);


    Optional<ProductEntity> findByCode(String code);

    boolean existsByIsbn(String isbn);
}
