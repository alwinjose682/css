package io.alw.css.fosimulator.template.model;

import java.util.List;

public record PrimaryAndSecondaryAmendmentSubjectContexts(
        AmendmentSubjectContext primaryAmendmentSubjectContext,
        List<AmendmentSubjectContext> secondaryAmendmentSubjectContexts
) {
}
