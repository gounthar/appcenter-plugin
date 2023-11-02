package io.jenkins.plugins.appcenter.validator;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class UsernameValidator extends Validator {

    @NonNull
    @Override
    protected Predicate<String> predicate() {
        return Pattern.compile("^(?![a-zA-Z0-9-_.]*+$)")
                .asPredicate()
                .negate();
    }

}