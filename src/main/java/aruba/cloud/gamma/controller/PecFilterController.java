package aruba.cloud.gamma.controller;

import aruba.cloud.gamma.dto.pec.PecFilterDTO;
import aruba.cloud.gamma.service.PecFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/pec-filters")
@RequiredArgsConstructor
@Tag(name = "Pec Filters", description = "Endpoint per la gestione dei filtri PEC")
public class PecFilterController {

    private final PecFilterService pecFilterService;

    @Operation(summary = "Ottieni filtri per tenant", description = "Restituisce tutti i filtri PEC associati a un determinato Tenant ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtri recuperati con successo")
    })
    @GetMapping("pec-by-tenant/{tenantId}")
    public ResponseEntity<List<PecFilterDTO>> getFiltersByTenant(@PathVariable String tenantId){
        List<PecFilterDTO> dtoList = pecFilterService.getFiltersByTenant(tenantId);
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "Crea un nuovo filtro", description = "Salva un nuovo filtro PEC a database per un determinato Tenant ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Filtro creato con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta malformata (es. campi obbligatorori mancanti)")
    })
    @PostMapping("create")
    public ResponseEntity<PecFilterDTO> createFilter(@RequestBody PecFilterDTO dto){
        PecFilterDTO createdDto = pecFilterService.createFilter(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    @Operation(summary = "Elimina un filtro", description = "Elimina fisicamente dal database un filtro PEC dato il suo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Filtro eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Filtro non trovato con l'ID specificato")
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteFilters(@PathVariable Long id){
        boolean deleted = pecFilterService.deleteFilter(id);
        if(deleted) {
            return ResponseEntity.noContent().build();
        } else return ResponseEntity.notFound().build();
    }

 }
