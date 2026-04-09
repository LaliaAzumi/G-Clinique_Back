package com.erp.clinique.controller;

import com.erp.clinique.dto.RevenuMensuelDTO;
import com.erp.clinique.repository.OrdonnanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;   
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin("*") // Pour que ton Front puisse appeler l'API
public class StatistiqueController {

    @Autowired
    private OrdonnanceRepository ordonnanceRepository;

    @GetMapping("/revenus/{year}")
    public List<RevenuMensuelDTO> getRevenus(@PathVariable int year) {
        return ordonnanceRepository.findMonthlyRevenueByYear(year);
    }
}