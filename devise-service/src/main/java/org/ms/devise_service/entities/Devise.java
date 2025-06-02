package org.ms.devise_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Devise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // Ex: EUR, USD, MAD

    private String name;

    private double tauxChange; // Taux par rapport à la devise de référence

    private boolean deviseReference; // true si c’est la devise principale
}
