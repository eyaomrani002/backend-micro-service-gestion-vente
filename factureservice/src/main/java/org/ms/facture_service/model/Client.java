package org.ms.facture_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Client {
    private Long id;
    private String name;
    private String email;
    private String adresse;
}