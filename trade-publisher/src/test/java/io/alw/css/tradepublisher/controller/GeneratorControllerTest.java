package io.alw.css.tradepublisher.controller;

import io.alw.css.tradepublisher.generator.GeneratorHandlerOutcomeDtoBuilder;
import io.alw.css.tradepublisher.trade.model.GeneratorInitialValues;
import io.alw.css.tradepublisher.trade.service.GeneratorService;
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
@ContextConfiguration(classes = GeneratorController.class)
class GeneratorControllerTest {

    @Captor
    ArgumentCaptor<GeneratorInitialValues> cfGeneratorValCaptor;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GeneratorService generatorService;

    @Test
    void start() throws Exception {
        var SUCCESS = "Success";
        var successOutcome = GeneratorHandlerOutcomeDtoBuilder.builder().msgs(List.of(SUCCESS)).build();

        //given
        when(generatorService.start(any(GeneratorInitialValues.class)))
                .thenReturn(successOutcome);
        //when-then
        mockMvc.perform(MockMvcRequestBuilders.put(GeneratorController.CF_GEN_URL + "/start/" + GeneratorController.ALL_GENERATORS_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valueDate":"2026-07-08",
                                "tradeId":100054321,
                                "matchStatusEventId":54321}
                                """))
                .andExpect(jsonPath("$.msgs").value(SUCCESS))
                .andExpect(status().isAccepted())
                .andReturn();

        verify(generatorService).start(cfGeneratorValCaptor.capture());
        GeneratorInitialValues cfGeneratorInitialValues = cfGeneratorValCaptor.getValue();
        assertThat(cfGeneratorInitialValues.valueDate()).isEqualTo("2026-07-08");
        assertThat(cfGeneratorInitialValues.tradeId()).isEqualTo(100054321);
        assertThat(cfGeneratorInitialValues.matchStatusEventId()).isEqualTo(54321);
    }
}
