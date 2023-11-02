package io.jenkins.plugins.appcenter.validator;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;

import java.util.function.Predicate;

public final class PathToAppValidator extends Validator {

    @NonNull
    @Override
    protected Predicate<String> predicate() {
        return Util::isRelativePath;
    }
}