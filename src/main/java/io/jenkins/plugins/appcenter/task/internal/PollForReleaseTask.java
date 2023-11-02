package io.jenkins.plugins.appcenter.task.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import io.jenkins.plugins.appcenter.AppCenterException;
import io.jenkins.plugins.appcenter.AppCenterLogger;
import io.jenkins.plugins.appcenter.api.AppCenterServiceFactory;
import io.jenkins.plugins.appcenter.task.request.UploadRequest;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Singleton
public final class PollForReleaseTask implements AppCenterTask<UploadRequest>, AppCenterLogger {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final TaskListener taskListener;
    @NonNull
    private final AppCenterServiceFactory factory;

    @Inject
    PollForReleaseTask(@NonNull final TaskListener taskListener,
                       @NonNull final AppCenterServiceFactory factory) {
        this.taskListener = taskListener;
        this.factory = factory;
    }

    @NonNull
    @Override
    public CompletableFuture<UploadRequest> execute(@NonNull UploadRequest request) {
        return pollForRelease(request);
    }

    @NonNull
    private CompletableFuture<UploadRequest> pollForRelease(@NonNull UploadRequest request) {
        final String uploadId = requireNonNull(request.uploadId, "uploadId cannot be null");

        log("Polling for app release.");

        final CompletableFuture<UploadRequest> future = new CompletableFuture<>();

        poll(request, uploadId, future);

        return future;
    }

    private void poll(@NonNull UploadRequest request, @NonNull String uploadId, @NonNull CompletableFuture<UploadRequest> future) {
        poll(request, uploadId, future, 0);
    }

    private void poll(@NonNull UploadRequest request, @NonNull String uploadId, @NonNull CompletableFuture<UploadRequest> future, int timeoutExponent) {
        factory.createAppCenterService()
            .pollForRelease(request.ownerName, request.appName, uploadId)
            .whenComplete((pollForReleaseResponse, throwable) -> {
                if (throwable != null) {
                    final AppCenterException exception = logFailure("Polling for app release unsuccessful", throwable);
                    future.completeExceptionally(exception);
                } else {
                    switch (pollForReleaseResponse.upload_status) {
                        case uploadStarted:
                        case uploadFinished:
                            retryPolling(request, uploadId, future, timeoutExponent);
                            break;
                        case readyToBePublished:
                            log("Polling for app release successful.");
                            final UploadRequest uploadRequest = request.newBuilder()
                                .setReleaseId(pollForReleaseResponse.release_distinct_id)
                                .build();
                            future.complete(uploadRequest);
                            break;
                        case malwareDetected:
                        case error:
                            future.completeExceptionally(logFailure("Polling for app release successful however was rejected by server: " + pollForReleaseResponse.error_details));
                            break;
                        default:
                            future.completeExceptionally(logFailure("Polling for app release successful however unexpected enum returned from server: " + pollForReleaseResponse.upload_status));
                    }
                }
            });
    }

    private void retryPolling(@NonNull UploadRequest request, @NonNull String uploadId, @NonNull CompletableFuture<UploadRequest> future, int timeoutExponent) {
        try {
            final double pow = Math.pow(2, timeoutExponent);
            final long timeout = Double.valueOf(pow).longValue();
            log(String.format("Polling for app release successful however not yet ready will try again in %d seconds.", timeout));
            TimeUnit.SECONDS.sleep(timeout);
            poll(request, uploadId, future, timeoutExponent + 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PrintStream getLogger() {
        return taskListener.getLogger();
    }
}