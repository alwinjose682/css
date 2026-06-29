package io.alw.css.fosimulator;

import io.alw.css.domain.trade.Trade;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.junit.jupiter.api.Test;

/// NOTE: Schema generated like this is further modified manually where required
public class avro {
    @Test
    void getAvroSchema_notATest() {
        Schema schema = ReflectData.get()
                .getSchema(Trade.class)
//                .getSchema(TradeLeg.class)
                ;

        System.out.println(schema);
    }
}
