package io.jenkins.plugins.appcenter.validator;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.function.Predicate;

public final class AppNameValidator extends Validator {

    @NonNull
    @Override
    protected Predicate<String> predicate() {
        return value -> !value.contains(" ");
    }

}