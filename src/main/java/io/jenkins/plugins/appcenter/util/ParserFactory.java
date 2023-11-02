package io.jenkins.plugins.appcenter.util;

import edu.umd.cs.findbugs.annotations.NonNull;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;

public final class ParserFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    ParserFactory() {
    }

    @NonNull
    public AndroidParser androidParser(final @NonNull File file) {
        return new AndroidParser(file);
    }
}