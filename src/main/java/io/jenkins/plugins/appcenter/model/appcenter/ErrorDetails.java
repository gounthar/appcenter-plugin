package io.jenkins.plugins.appcenter.model.appcenter;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;

public final class ErrorDetails {

    @NonNull
    public final CodeEnum code;

    @NonNull
    public final String message;

    public ErrorDetails(@NonNull CodeEnum code, @NonNull String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorDetails{" +
            "code=" + code +
            ", message='" + message + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorDetails that = (ErrorDetails) o;
        return code == that.code &&
            message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }

    public enum CodeEnum {
        BadRequest,
        Conflict,
        NotAcceptable,
        NotFound,
        InternalServerError,
        Unauthorized,
        TooManyRequests
    }
}