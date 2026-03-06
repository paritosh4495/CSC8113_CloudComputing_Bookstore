package com.group1.cartservice.domain;

import com.group1.cartservice.domain.model.CartStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "carts")
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_seq")
    @SequenceGenerator(name = "cart_seq", sequenceName = "carts_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItemEntity> items = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private CartStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Helper method to add item to cart and maintain bidirectional relationship

    public void addItem(CartItemEntity item) {
        if(this.items == null) {
            this.items = new HashSet<>();
        }
        this.items.add(item);
        item.setCart(this);
    }

        // Helper method to remove item from cart and maintain bidirectional relationship
    public void removeItem(CartItemEntity item) {
        if(this.items != null) {
            this.items.remove(item);
            item.setCart(null);
        }
    }


}
