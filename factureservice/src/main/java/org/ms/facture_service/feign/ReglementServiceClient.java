package org.ms.facture_service.feign;

import org.ms.facture_service.security.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "reglement-service", configuration = FeignClientConfig.class)
public interface ReglementServiceClient {
    @GetMapping("/reglements/facture/{factureId}/sum")
    Double sumByFactureId(@PathVariable("factureId") Long factureId);
}