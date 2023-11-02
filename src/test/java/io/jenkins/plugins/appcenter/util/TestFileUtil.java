package io.jenkins.plugins.appcenter.util;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;

public final class TestFileUtil {

    public static final String TEST_FILE_PATH = "src/test/resources/three/days/xiola.apk";

    @NonNull
    public static File createFileForTesting() {
        return new File(TEST_FILE_PATH);
    }

    @NonNull
    public static File createLargeFileForTesting() {
        return new File(TEST_FILE_PATH) {
            @Override
            public long length() {
                return (1024 * 1024) * 512; // Double the max size allowed to upload.
            }
        };
    }
}