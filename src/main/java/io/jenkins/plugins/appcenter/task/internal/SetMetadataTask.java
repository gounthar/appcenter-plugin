package io.jenkins.plugins.appcenter.task.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import io.jenkins.plugins.appcenter.AppCenterException;
import io.jenkins.plugins.appcenter.AppCenterLogger;
import io.jenkins.plugins.appcenter.api.AppCenterServiceFactory;
import io.jenkins.plugins.appcenter.task.request.UploadRequest;
import io.jenkins.plugins.appcenter.util.RemoteFileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

@Singleton
public final class SetMetadataTask implements AppCenterTask<UploadRequest>, AppCenterLogger {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final TaskListener taskListener;
    @NonNull
    private final AppCenterServiceFactory factory;
    @NonNull
    private final RemoteFileUtils remoteFileUtils;

    @Inject
    SetMetadataTask(@NonNull final TaskListener taskListener,
                    @NonNull final AppCenterServiceFactory factory,
                    @NonNull final RemoteFileUtils remoteFileUtils) {
        this.taskListener = taskListener;
        this.factory = factory;
        this.remoteFileUtils = remoteFileUtils;
    }

    @NonNull
    @Override
    public CompletableFuture<UploadRequest> execute(@NonNull UploadRequest request) {
        return setMetadata(request);
    }

    @NonNull
    private CompletableFuture<UploadRequest> setMetadata(@NonNull UploadRequest request) {
        final String uploadDomain = requireNonNull(request.uploadDomain, "uploadDomain cannot be null");
        final String packageAssetId = requireNonNull(request.packageAssetId, "packageAssetId cannot be null");
        final String token = requireNonNull(request.token, "token cannot be null");

        log("Setting metadata.");

        final CompletableFuture<UploadRequest> future = new CompletableFuture<>();

        final String url = getUrl(request.pathToApp, uploadDomain, packageAssetId, token);

        factory.createAppCenterService()
            .setMetaData(url)
            .whenComplete((setMetadataResponse, throwable) -> {
                if (throwable != null) {
                    final AppCenterException exception = logFailure("Setting metadata unsuccessful", throwable);
                    future.completeExceptionally(exception);
                } else {
                    log("Setting metadata successful.");

                    final UploadRequest uploadRequest = request.newBuilder()
                        .setChunkSize(setMetadataResponse.chunk_size)
                        .build();
                    future.complete(uploadRequest);
                }
            });

        return future;
    }


    @NonNull
    private String getUrl(@NonNull String pathToApp, @NonNull String uploadDomain, @NonNull String packageAssetId, @NonNull String token) {
        final String fileName = remoteFileUtils.getFileName(pathToApp);
        final long fileSize = remoteFileUtils.getFileSize(pathToApp);
        final String contentType = remoteFileUtils.getContentType(pathToApp);

        return String.format("%1$s/upload/set_metadata/%2$s?file_name=%3$s&file_size=%4$d&token=%5$s&content_type=%6$s", uploadDomain, packageAssetId, fileName, fileSize, token, contentType);
    }

    @Override
    public PrintStream getLogger() {
        return taskListener.getLogger();
    }
}