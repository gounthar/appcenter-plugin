package io.jenkins.plugins.appcenter.validator;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.function.Predicate;

public abstract class Validator {

    @NonNull
    protected abstract Predicate<String> predicate();

    public boolean isValid(@NonNull String value) {
        return predicate().test(value);
    }
}