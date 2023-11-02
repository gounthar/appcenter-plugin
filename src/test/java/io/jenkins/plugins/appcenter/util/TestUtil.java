package io.jenkins.plugins.appcenter.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.util.Objects;

public final class TestUtil {
    public static TestBuilder createFile(final @NonNull String pathToFile) {
        return createFile(pathToFile, "all of us with wings");
    }

    public static TestBuilder createFile(final @NonNull String pathToFile, final @NonNull String content) {
        return new TestAppWriter(pathToFile, content);
    }

    private static class TestAppWriter extends TestBuilder {

        @NonNull
        private final String pathToFile;
        @NonNull
        private final String content;

        private TestAppWriter(final @NonNull String pathToFile, final @NonNull String content) {
            this.pathToFile = pathToFile;
            this.content = content;
        }

        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
            Objects.requireNonNull(build.getWorkspace()).child(pathToFile).write(content, "UTF-8");
            return true;
        }
    }
}