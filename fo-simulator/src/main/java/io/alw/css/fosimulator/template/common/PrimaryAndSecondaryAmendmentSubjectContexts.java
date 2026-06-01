package io.alw.css.fosimulator.template.common;

import java.util.List;

public record PrimaryAndSecondaryAmendmentSubjectContexts(
        AmendmentSubjectContext primaryAmendmentSubjectContext,
        List<AmendmentSubjectContext> secondaryAmendmentSubjectContexts
) {
}
