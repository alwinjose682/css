package io.alw.css.tradepublisher.generator;

import io.alw.css.tradepublisher.trade.model.GeneratorDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public final class Generator<T> extends Stoppable implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(Generator.class);
    private final Supplier<List<T>> supplier;
    private final Consumer<List<T>> publisher;
    private final GeneratorDetail generatorDetail;
    private final long pauseIntervalSeconds;
    private final RandomGenerator rndm;

    public Generator(GeneratorDetail generatorDetail, Supplier<List<T>> supplier, Consumer<List<T>> publisher) {
        super();
        this.generatorDetail = generatorDetail;
        this.supplier = supplier;
        this.pauseIntervalSeconds = generatorDetail.generationFrequency() * 1_000;
        this.rndm = RandomGenerator.getDefault();
        this.publisher = publisher;
    }

    @Override
    public void run() {
        try {
            // Before actual start, pauses the generator for a random small amount of time so that all the generators do not appear to start at the same time when checking the logs
            long pauseTimeBeforeActualStart = rndm.nextLong(0, pauseIntervalSeconds);
            Thread.sleep(pauseTimeBeforeActualStart);
            // Start
            while (!isStopSignalled()) {
                List<T> trades = supplier.get();
                publisher.accept(trades);
                Thread.sleep(pauseIntervalSeconds);
            }
        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // TODO and remove below throw of runtimeException
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Exception occurred for generator: {}, generationFrequency: {}. Exception: {}", generatorDetail.generatorKey(), generatorDetail.generationFrequency(), e.getMessage(), e);
            e.printStackTrace();
        } finally {
            markTaskExecutionIsCompleted();
        }
    }

    @Override
    protected void markTaskExecutionIsCompleted() {
        setTaskExecutionAsCompleted();
    }
}
