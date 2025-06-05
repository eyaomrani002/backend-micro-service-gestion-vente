package org.ms.reglement_service.feign;

import org.ms.reglement_service.model.Devise;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "devise-service")
public interface DeviseServiceClient {
    @GetMapping("/devises/{code}")
    Devise findDeviseByCode(@PathVariable("code") String code);
}
