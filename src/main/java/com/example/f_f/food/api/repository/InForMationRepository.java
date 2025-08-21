package com.example.f_f.food.api.repository;

import com.example.f_f.food.api.entity.InForMation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InForMationRepository extends JpaRepository<InForMation, String> {
}
