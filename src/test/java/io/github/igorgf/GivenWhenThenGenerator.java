package io.github.igorgf;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class GivenWhenThenGenerator extends DisplayNameGenerator.Standard {

    @Override
    public String generateDisplayNameForClass(
            Class<?> testClass
    ) {
        GivenWhenThen ann = testClass.getAnnotation(GivenWhenThen.class);
        return ann != null
                ? build(ann)
                : super.generateDisplayNameForClass(testClass);
    }

    @Override
    public String generateDisplayNameForNestedClass(
            @NonNull List<Class<?>> enclosingInstanceTypes,
            Class<?> nestedClass
    ) {
        GivenWhenThen ann = nestedClass.getAnnotation(GivenWhenThen.class);
        return ann != null
                ? build(ann)
                : super.generateDisplayNameForNestedClass(enclosingInstanceTypes, nestedClass);
    }

    @Override
    public String generateDisplayNameForMethod(
            @NonNull List<Class<?>> enclosingInstanceTypes,
            @NonNull Class<?> testClass, Method testMethod
    ) {
        GivenWhenThen ann = testMethod.getAnnotation(GivenWhenThen.class);
        return ann != null
                ? build(ann)
                : super.generateDisplayNameForMethod(enclosingInstanceTypes, testClass, testMethod);
    }

    private String build(GivenWhenThen ann) {
        StringBuilder sb = new StringBuilder();
        appendClause(sb, "Given", ann.given());
        appendClause(sb, "When",  ann.when());
        appendClause(sb, "Then",  ann.then());
        return sb.toString().trim();
    }

    private void appendClause(StringBuilder sb, String label, String[] values) {
        if (values.length == 0) return;
        String joined = String.join(", ", values);
        sb.append(label).append(" ").append(joined).append(". ");
    }

}
