package io.jenkins.plugins.appcenter.model.appcenter;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;

public final class SetMetadataResponse {
    @NonNull
    public final Integer chunk_size;

    public SetMetadataResponse(@NonNull Integer chunkSize) {
        this.chunk_size = chunkSize;
    }

    @Override
    public String toString() {
        return "SetMetadataResponse{" +
            "chunk_size=" + chunk_size +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetMetadataResponse that = (SetMetadataResponse) o;
        return chunk_size.equals(that.chunk_size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunk_size);
    }
}