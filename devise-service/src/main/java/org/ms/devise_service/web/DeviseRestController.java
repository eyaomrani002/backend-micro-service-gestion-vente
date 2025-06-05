package org.ms.devise_service.web;

import org.ms.devise_service.entities.Devise;
import org.ms.devise_service.repository.DeviseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/devises")
public class DeviseRestController {

    private final DeviseRepository deviseRepository;

    public DeviseRestController(DeviseRepository deviseRepository) {
        this.deviseRepository = deviseRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public List<Devise> getAllDevises() {
        return deviseRepository.findAll();
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Devise> getDeviseById(@PathVariable Long id) {
        return deviseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Devise> getDeviseByCode(@PathVariable String code) {
        Devise devise = deviseRepository.findByCode(code.toUpperCase());
        if (devise == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(devise);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Devise> createDevise(@RequestBody Devise devise) {
        if (devise.getCode() == null || !devise.getCode().matches("^[A-Z]{3}$")) {
            return ResponseEntity.badRequest().body(null);
        }
        devise.setCode(devise.getCode().toUpperCase());
        Devise saved = deviseRepository.save(devise);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }


    @GetMapping("/convert/{montant}/{from}/{to}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Double> convertDevise(
            @PathVariable double montant,
            @PathVariable String from,
            @PathVariable String to) {

        Devise deviseFrom = deviseRepository.findByCode(from.toUpperCase());
        Devise deviseTo = deviseRepository.findByCode(to.toUpperCase());

        if (deviseFrom == null || deviseTo == null) {
            return ResponseEntity.badRequest().build();
        }

        double result = montant * (deviseTo.getTauxChange() / deviseFrom.getTauxChange());
        return ResponseEntity.ok(result);
    }
}
