package io.jenkins.plugins.appcenter.model.appcenter;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;

public final class SymbolUploadBeginResponse {
    @NonNull
    public final String symbol_upload_id;
    @NonNull
    public final String upload_url;
    @NonNull
    public final String expiration_date;

    public SymbolUploadBeginResponse(@NonNull String symbolUploadId, @NonNull String uploadUrl, @NonNull String expirationDate) {
        this.symbol_upload_id = symbolUploadId;
        this.upload_url = uploadUrl;
        this.expiration_date = expirationDate;
    }

    @Override
    public String toString() {
        return "SymbolUploadBeginResponse{" +
            "symbol_upload_id='" + symbol_upload_id + '\'' +
            ", upload_url='" + upload_url + '\'' +
            ", expiration_date='" + expiration_date + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolUploadBeginResponse that = (SymbolUploadBeginResponse) o;
        return symbol_upload_id.equals(that.symbol_upload_id) &&
            upload_url.equals(that.upload_url) &&
            expiration_date.equals(that.expiration_date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol_upload_id, upload_url, expiration_date);
    }
}