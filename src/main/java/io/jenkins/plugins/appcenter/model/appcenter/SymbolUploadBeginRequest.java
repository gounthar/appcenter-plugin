package io.jenkins.plugins.appcenter.model.appcenter;

import edu.umd.cs.findbugs.annotations.NonNull;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.Serializable;
import java.util.Objects;

public final class SymbolUploadBeginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    public final SymbolTypeEnum symbol_type;
    @Nullable
    public final String client_callback;
    @Nullable
    public final String file_name;
    @Nullable
    public final String build;
    @Nullable
    public final String version;

    public SymbolUploadBeginRequest(@NonNull SymbolTypeEnum symbolTypeEnum, @Nullable String clientCallback, @Nullable String fileName, @Nullable String build, @Nullable String version) {
        this.symbol_type = symbolTypeEnum;
        this.client_callback = clientCallback;
        this.file_name = fileName;
        this.build = build;
        this.version = version;
    }

    @Override
    public String toString() {
        return "SymbolUploadBeginRequest{" +
            "symbol_type=" + symbol_type +
            ", client_callback='" + client_callback + '\'' +
            ", file_name='" + file_name + '\'' +
            ", build='" + build + '\'' +
            ", version='" + version + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolUploadBeginRequest that = (SymbolUploadBeginRequest) o;
        return symbol_type == that.symbol_type &&
            Objects.equals(client_callback, that.client_callback) &&
            Objects.equals(file_name, that.file_name) &&
            Objects.equals(build, that.build) &&
            Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol_type, client_callback, file_name, build, version);
    }

    public enum SymbolTypeEnum {
        Apple,
        JavaScript,
        Breakpad,
        AndroidProguard,
        UWP
    }
}