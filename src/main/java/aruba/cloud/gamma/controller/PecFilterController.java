package aruba.cloud.gamma.controller;

import aruba.cloud.gamma.dto.pec.PecFilterDTO;
import aruba.cloud.gamma.service.PecFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/pec-filters")
@RequiredArgsConstructor
public class PecFilterController {

    private final PecFilterService pecFilterService;

    @GetMapping("pec-by-tenant/{tenantId}")
    public ResponseEntity<List<PecFilterDTO>> getFiltersByTenant(@PathVariable String tenantId){
        List<PecFilterDTO> dtoList = pecFilterService.getFiltersByTenant(tenantId);
        return ResponseEntity.ok(dtoList);
    }

 }
