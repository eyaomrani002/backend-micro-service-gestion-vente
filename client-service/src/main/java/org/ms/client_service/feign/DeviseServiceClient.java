package org.ms.client_service.feign;

import org.ms.client_service.security.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "DEVISE-SERVICE", configuration = FeignClientConfig.class)
public interface DeviseServiceClient {
    @GetMapping("/devises/convert/{montant}/{from}/{to}")
    Double convertDevise(@PathVariable("montant") Double montant,
                        @PathVariable("from") String from,
                        @PathVariable("to") String to);
}