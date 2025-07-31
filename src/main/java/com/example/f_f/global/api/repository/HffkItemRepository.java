package com.example.f_f.global.api.repository;

import com.example.f_f.global.api.entity.HffkItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HffkItemRepository extends JpaRepository<HffkItem, String> {
}
