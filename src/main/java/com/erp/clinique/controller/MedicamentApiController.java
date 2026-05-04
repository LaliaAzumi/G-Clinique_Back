package com.erp.clinique.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import com.erp.clinique.model.Medicament;
import com.erp.clinique.repository.MedicamentRepository;

@RestController
@RequestMapping("/api/v1/medicaments")
@CrossOrigin(origins = "*")
public class MedicamentApiController {

    @Autowired
    private MedicamentRepository medicamentRepository;

    // LIST
    @GetMapping
    public Page<Medicament> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.isEmpty()) {
            return medicamentRepository.searchAll(keyword, pageable);
        }

        System.out.println(medicamentRepository.findAll());

        return medicamentRepository.findAll(pageable);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Medicament get(@PathVariable Long id) {
        return medicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Introuvable"));
    }

    // SAVE
    @PostMapping("/save")
    public Medicament save(@RequestBody Medicament medicament) {
        return medicamentRepository.save(medicament);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicamentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // SEARCH
    @GetMapping("/search")
    public List<Medicament> search(@RequestParam String keyword) {
        return medicamentRepository.searchAll(keyword, PageRequest.of(0, 10)).getContent();
    }

    @PutMapping("/{id}")
    public Medicament update(@PathVariable Long id, @RequestBody Medicament m) {
        Medicament existing = medicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médicament introuvable"));

        existing.setNom(m.getNom());
        existing.setDescription(m.getDescription());
        existing.setPu(m.getPu());
        existing.setQStock(m.getQStock());

        return medicamentRepository.save(existing);
    }
}