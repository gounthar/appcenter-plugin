package io.jenkins.plugins.appcenter.validator;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.function.Predicate;

public final class BranchNameValidator extends Validator {

    @NonNull
    @Override
    protected Predicate<String> predicate() {
        return value -> !value.contains(" ");
    }

}