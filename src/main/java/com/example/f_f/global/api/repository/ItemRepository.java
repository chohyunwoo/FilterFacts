package com.example.f_f.global.api.repository;

import com.example.f_f.global.api.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, String> {
}
