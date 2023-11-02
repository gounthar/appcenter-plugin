package io.jenkins.plugins.appcenter.task.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import io.jenkins.plugins.appcenter.AppCenterException;
import io.jenkins.plugins.appcenter.AppCenterLogger;
import io.jenkins.plugins.appcenter.api.AppCenterServiceFactory;
import io.jenkins.plugins.appcenter.model.appcenter.SymbolUploadEndRequest;
import io.jenkins.plugins.appcenter.task.request.UploadRequest;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

@Singleton
public final class FinishReleaseTask implements AppCenterTask<UploadRequest>, AppCenterLogger {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final TaskListener taskListener;
    @NonNull
    private final AppCenterServiceFactory factory;

    @Inject
    FinishReleaseTask(@NonNull final TaskListener taskListener,
                      @NonNull final AppCenterServiceFactory factory) {
        this.taskListener = taskListener;
        this.factory = factory;
    }

    @NonNull
    @Override
    public CompletableFuture<UploadRequest> execute(@NonNull UploadRequest request) {


        if (request.symbolUploadId == null) {
            return finishRelease(request);
        } else {
            return finishRelease(request)
                .thenCompose(this::finishSymbolRelease);
        }
    }

    @NonNull
    private CompletableFuture<UploadRequest> finishRelease(@NonNull UploadRequest request) {
        final String uploadDomain = requireNonNull(request.uploadDomain, "uploadDomain cannot be null");
        final String packageAssetId = requireNonNull(request.packageAssetId, "packageAssetId cannot be null");
        final String token = requireNonNull(request.token, "token cannot be null");

        log("Finishing release.");

        final CompletableFuture<UploadRequest> future = new CompletableFuture<>();

        final String url = getUrl(uploadDomain, packageAssetId, token);

        factory.createAppCenterService()
            .finishRelease(url)
            .whenComplete((finishReleaseResponse, throwable) -> {
                if (throwable != null) {
                    final AppCenterException exception = logFailure("Finishing release unsuccessful", throwable);
                    future.completeExceptionally(exception);
                } else {
                    log("Finishing release successful.");
                    future.complete(request);
                }
            });

        return future;
    }

    @NonNull
    private String getUrl(@NonNull String uploadDomain, @NonNull String packageAssetId, @NonNull String token) {
        return String.format("%1$s/upload/finished/%2$s?token=%3$s", uploadDomain, packageAssetId, token);
    }

    @NonNull
    private CompletableFuture<UploadRequest> finishSymbolRelease(@NonNull UploadRequest request) {
        final String symbolUploadId = requireNonNull(request.symbolUploadId, "symbolUploadId cannot be null");

        log("Finishing symbol release.");

        final CompletableFuture<UploadRequest> future = new CompletableFuture<>();
        final SymbolUploadEndRequest symbolUploadEndRequest = new SymbolUploadEndRequest(SymbolUploadEndRequest.StatusEnum.committed);

        factory.createAppCenterService()
            .symbolUploadsComplete(request.ownerName, request.appName, symbolUploadId, symbolUploadEndRequest)
            .whenComplete((symbolUploadEndResponse, throwable) -> {
                if (throwable != null) {
                    final AppCenterException exception = logFailure("Finishing symbol release unsuccessful: ", throwable);
                    future.completeExceptionally(exception);
                } else {
                    log("Finishing symbol release successful.");
                    future.complete(request);
                }
            });

        return future;
    }

    @Override
    public PrintStream getLogger() {
        return taskListener.getLogger();
    }
}