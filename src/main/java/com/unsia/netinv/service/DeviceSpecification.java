package com.unsia.netinv.service;

import org.springframework.data.jpa.domain.Specification;

import com.unsia.netinv.entity.Device;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class DeviceSpecification implements Specification<Device> {

    private String search;
    private String status;
    private String type;

    @Override
    public Predicate toPredicate(Root<Device> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        
        Predicate predicate = cb.conjunction();

        if (search != null && !search.isEmpty()) {
            Predicate searchPredicate = cb.or(
                cb.like(cb.lower(root.get("deviceName")), "%" + search.toLowerCase() + "%"),
                cb.like(root.get("ipAddress"), "%" + search + "%"),
                cb.like(cb.lower(root.join("location").get("locationName")), "%" + search.toLowerCase() + "%")
            );
            predicate = cb.and(predicate, searchPredicate);
        }

        if (status != null && !status.isEmpty()) {
            predicate = cb.and(predicate, cb.equal(root.get("statusDevice"), status));
        }

        if (type != null && !type.isEmpty()) {
            predicate = cb.and(predicate, cb.equal(root.get("deviceType"), type));
        }

        return predicate;
    }
    
}
