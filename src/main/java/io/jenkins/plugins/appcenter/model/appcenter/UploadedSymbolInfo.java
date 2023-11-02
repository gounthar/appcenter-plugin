package io.jenkins.plugins.appcenter.model.appcenter;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;

public final class UploadedSymbolInfo {
    @NonNull
    public final String symbol_id;
    @NonNull
    public final String platform;

    public UploadedSymbolInfo(@NonNull String symbolId, @NonNull String platform) {
        this.symbol_id = symbolId;
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "UploadedSymbolInfo{" +
            "symbol_id='" + symbol_id + '\'' +
            ", platform='" + platform + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadedSymbolInfo that = (UploadedSymbolInfo) o;
        return symbol_id.equals(that.symbol_id) &&
            platform.equals(that.platform);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol_id, platform);
    }
}