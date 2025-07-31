package com.example.f_f.global.api.controller;

import com.example.f_f.global.api.service.DrugItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drug")
@RequiredArgsConstructor
public class DrugItemController {

    private final DrugItemService drugItemService;

    @GetMapping("/fetch")
    public String fetchDrugData(
            @RequestParam String itemName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "5") Integer numOfRows
    ) {
        drugItemService.fetchAndSaveDrugItems(itemName, startDate, endDate, pageNo, numOfRows);
        return "데이터 수집 완료";
    }
}