package io.jenkins.plugins.appcenter.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.dongliu.apk.parser.ApkParsers;
import net.dongliu.apk.parser.bean.ApkMeta;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.File;
import java.io.IOException;

public final class AndroidParser {

    @NonNull
    private final File file;
    @Nullable
    private ApkMeta apkMeta;

    AndroidParser(final @NonNull File file) {
        this.file = file;
    }

    @NonNull
    public String versionCode() throws IOException {
        return metaInfo().getVersionCode().toString();
    }

    @NonNull
    public String versionName() throws IOException {
        return metaInfo().getVersionName();
    }

    @NonNull
    public String fileName() {
        return file.getName();
    }

    @NonNull
    private ApkMeta metaInfo() throws IOException {
        if (apkMeta != null) return apkMeta;

        apkMeta = ApkParsers.getMetaInfo(file);

        return apkMeta;
    }
}