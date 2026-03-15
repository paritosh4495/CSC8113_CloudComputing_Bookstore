package com.group1.catalogservice.domain.model;

import com.group1.catalogservice.domain.ProductEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<ProductEntity> search(
            String query,
            String genre,
            String author,
            String name,
            String isbn,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ){
        return (root, cq,cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Dont include Discontinued products

            predicates.add(cb.notEqual(root.get("status"),Status.DISCONTINUED));

            if (query != null && !query.isBlank()) {
                String likeQuery = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")),        likeQuery),
                        cb.like(cb.lower(root.get("description")), likeQuery),
                        cb.like(cb.lower(root.get("author")),      likeQuery),
                        cb.like(cb.lower(root.get("isbn")),        likeQuery)
                ));
            }
            if (genre != null && !genre.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("genre")), genre.toLowerCase()));
            }
            if (author != null && !author.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (isbn != null && !isbn.isBlank()) {
                predicates.add(cb.equal(root.get("isbn"), isbn));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return  cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
