package aruba.cloud.gamma.controller;

import aruba.cloud.gamma.dto.pec.PecFilterDTO;
import aruba.cloud.gamma.service.PecFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("create")
    public ResponseEntity<PecFilterDTO> createFilter(@RequestBody PecFilterDTO dto){
        PecFilterDTO createdDto = pecFilterService.createFilter(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteFilters(@PathVariable Long id){
        boolean deleted = pecFilterService.deleteFilter(id);
        if(deleted) {
            return ResponseEntity.noContent().build();
        } else return ResponseEntity.notFound().build();
    }

 }
