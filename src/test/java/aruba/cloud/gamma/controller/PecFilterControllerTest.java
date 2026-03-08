package aruba.cloud.gamma.controller;

import aruba.cloud.gamma.dto.pec.PecFilterDTO;
import aruba.cloud.gamma.service.PecFilterService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@WebMvcTest(PecFilterController.class)
@AutoConfigureMockMvc(addFilters = false) //disabilitiamo la sicurezza di spring seecurity per i test unitari
public class PecFilterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PecFilterService pecFilterService;

    @Test
    public void testFilterByTenant() throws Exception {
        PecFilterDTO dto = PecFilterDTO.builder()
                .id(1L)
                .tenantId("tenant-1")
                .mailbox("test@pec.it")
                .folder("INBOX")
                .subjectFilter("Fattura")
                .build();

        Mockito.when(pecFilterService.getFiltersByTenant("tenant-1")).thenReturn(List.of(dto));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/pec-filters/pec-by-tenant/{tenantId}", "tenant-1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mailbox").value("test@pec.it"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].subjectFilter").value("Fattura"));
    }
}
