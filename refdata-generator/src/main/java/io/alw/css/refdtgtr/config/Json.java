package io.alw.css.refdtgtr.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Json {
    private static ObjectMapper objectMapper;

    public static ObjectMapper mapper() {
        if (objectMapper == null) {
            synchronized (Json.class) {
                if (objectMapper == null) {
                    objectMapper = createObjectMapper();
                }
                return objectMapper;
            }
        } else {
            return objectMapper;
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}

