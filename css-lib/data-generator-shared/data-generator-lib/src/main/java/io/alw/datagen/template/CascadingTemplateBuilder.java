package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CascadingTemplateBuilder<T extends TestDataGeneratable> extends TemplateBuilder<T, List<T>> {
    private int numOfChildTemplates;

    protected CascadingTemplateBuilder(T parent) {
        super(parent);
        this.numOfChildTemplates = 0;
    }

    protected abstract CascadingTemplateBuilder<T> childTemplate(T parent);

    /// Note: Invoking this method is optional. If not invoked ZERO related templates will be created
    public CascadingTemplateBuilder<T> childTemplate(int count) {
        this.numOfChildTemplates = count;
        return this;
    }

    /// Builds the parent template and its related templates, if any.
    /// The first element in the list is ALWAYS the parent. Rest of the elements build results of related templates.
    ///
    /// The related templates can access the build output, T, during their builds.
    /// Values of parentType such as io.alw.css.domain.referencedata.Counterparty#entityCode are required during the build of related templates
    ///
    /// Build creates the type based on the template and creates one or more related types if [TemplateBuilder#childTemplate(int)]>0.
    /// Related templates CAN have further related templates.
    @Override
    public List<T> build() {
        // 1. Build the template
        final T parent = finalBuildInstruction();
        List<T> result = new ArrayList<>();
        result.add(parent);

        // 2. Build child templates if any
        for (int idx = 0; idx < numOfChildTemplates; idx++) {
            List<T> relatedDefBuildResult = childTemplate(parent).build();
            result.addAll(relatedDefBuildResult);
        }

        return Collections.unmodifiableList(result);
    }
}
