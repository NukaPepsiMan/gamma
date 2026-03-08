package aruba.cloud.gamma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/pec-filters")
public class PecFilterController {

    @GetMapping("pec-by-tenant/{tenantId}")
    public ResponseEntity<String> getFiltersByTenant(@PathVariable String tenantId){
        return ResponseEntity.ok().build();
    }
 }
