package io.alw.css.refdtgtr.model;

import io.alw.css.domain.referencedata.Entity;
import io.alw.css.domain.referencedata.Nostro;

import java.util.List;

public record EntityAndDependentData(
        Entity entity,
        List<Nostro> nostros
) {
}