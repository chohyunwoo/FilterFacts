package com.example.f_f.global.api.repository;

import com.example.f_f.global.api.entity.DrugItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugItemRepository extends JpaRepository<DrugItem, Long> {
}