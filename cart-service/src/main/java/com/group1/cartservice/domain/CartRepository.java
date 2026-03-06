package com.group1.cartservice.domain;

import com.group1.cartservice.domain.model.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity,Long> {

    Optional<CartEntity> findByUserIdAndStatus(String userId, CartStatus status);
}
