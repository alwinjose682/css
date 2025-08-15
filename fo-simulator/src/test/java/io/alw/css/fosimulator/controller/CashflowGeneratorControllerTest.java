package io.alw.css.fosimulator.controller;

import io.alw.css.fosimulator.cashflowgnrtr.CashflowGeneratorHandlerOutcomeDtoBuilder;
import io.alw.css.fosimulator.model.CashflowGeneratorInitialValues;
import io.alw.css.fosimulator.service.CashflowGeneratorService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = CashflowGeneratorController.class)
class CashflowGeneratorControllerTest {

    @Captor
    ArgumentCaptor<CashflowGeneratorInitialValues> cfGeneratorValCaptor;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CashflowGeneratorService cashflowGeneratorService;

    @Test
    void start() throws Exception {
        var SUCCESS = "Success";
        var successOutcome = CashflowGeneratorHandlerOutcomeDtoBuilder.builder().msgs(List.of(SUCCESS)).build();

        //given
        when(cashflowGeneratorService.start(any(CashflowGeneratorInitialValues.class)))
                .thenReturn(successOutcome);
        //when-then
        mockMvc.perform(MockMvcRequestBuilders.put(CashflowGeneratorController.CF_GEN_URL + "/start/" + CashflowGeneratorController.ALL_GENERATORS_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valueDate":"2025-08-14",
                                "tradeId":1054321,
                                "foCashflowId":15432}
                                """))
                .andExpect(jsonPath("$.msgs").value(SUCCESS))
                .andExpect(status().isAccepted())
                .andReturn();

        verify(cashflowGeneratorService).start(cfGeneratorValCaptor.capture());
        CashflowGeneratorInitialValues cfGeneratorInitialValues = cfGeneratorValCaptor.getValue();
        assertThat(cfGeneratorInitialValues.valueDate()).isEqualTo("2025-08-14");
        assertThat(cfGeneratorInitialValues.tradeId()).isEqualTo(1054321);
        assertThat(cfGeneratorInitialValues.foCashflowId()).isEqualTo(15432);
    }
}
