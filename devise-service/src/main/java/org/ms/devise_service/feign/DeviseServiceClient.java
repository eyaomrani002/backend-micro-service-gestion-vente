/*package org.ms.devise_service.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.Data;

@FeignClient(name = "DEVISE-SERVICE")
public interface DeviseServiceClient {
    @GetMapping(path = "/devises/{id}")
    Devise findDeviseById(@PathVariable(name = "id") Long id);
}

@Data
class Devise {
    private Long id;
    private String code;
    private String name;
    private double exchangeRate;
}
*/