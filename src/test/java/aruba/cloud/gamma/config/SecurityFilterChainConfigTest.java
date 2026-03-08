package aruba.cloud.gamma.config;


import aruba.cloud.gamma.config.oauth2.SecurityFilterChainConfig;
import aruba.cloud.gamma.controller.PecFilterController;
import aruba.cloud.gamma.service.PecFilterService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

@WebMvcTest(PecFilterController.class)
@Import(SecurityFilterChainConfig.class)
public class SecurityFilterChainConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PecFilterService pecFilterService;

    @Test
    public void testSecurityFilterChain() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/pec-filters/pec-by-tenant/{tenantId}","test"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testJwtSecurityFilterChain() throws Exception {
        Mockito.when(pecFilterService.getFiltersByTenant("tenant-test")).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/pec-filters/pec-by-tenant/{tenantId}","test")
                .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
