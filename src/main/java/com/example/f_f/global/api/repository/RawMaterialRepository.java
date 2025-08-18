package com.example.f_f.global.api.repository;

import com.example.f_f.global.api.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawMaterialRepository extends JpaRepository<RawMaterial,String> {
}
