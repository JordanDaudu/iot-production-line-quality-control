package com.smartiot.qualityinspection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-context security tests proving role checks are enforced by the backend, not just
 * hidden in the UI (NFR-11, TC-E2E-10, TC-E2E-16, TC-E2E-17).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccessControlIntegrationTest {

    private static final String VALID_THRESHOLD =
            "{\"minValue\":90,\"warnMinValue\":95,\"warnMaxValue\":105,\"maxValue\":110,\"unit\":\"g\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCannotEditThresholds() throws Exception {
        mockMvc.perform(put("/api/thresholds/WEIGHT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_THRESHOLD))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void administratorCanEditThresholds() throws Exception {
        mockMvc.perform(put("/api/thresholds/WEIGHT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_THRESHOLD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "QUALITY_MANAGER")
    void qualityManagerCannotControlSimulation() throws Exception {
        mockMvc.perform(post("/api/simulation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCannotControlSimulation() throws Exception {
        mockMvc.perform(post("/api/simulation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCannotManageAlerts() throws Exception {
        mockMvc.perform(post("/api/alerts/1/acknowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "QUALITY_MANAGER")
    void qualityManagerCanManageAlerts() throws Exception {
        // Authorization passes (not 403); the alert does not exist, so the service 404s.
        mockMvc.perform(post("/api/alerts/999999/acknowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MAINTENANCE_TECHNICIAN")
    void maintenanceTechnicianCannotViewQualityAnalytics() throws Exception {
        mockMvc.perform(get("/api/dashboard/spc"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void operatorCanViewQualityAnalytics() throws Exception {
        mockMvc.perform(get("/api/dashboard/spc"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/thresholds/WEIGHT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());
    }
}
