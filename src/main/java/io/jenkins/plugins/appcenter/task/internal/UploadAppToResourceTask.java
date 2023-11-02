package io.jenkins.plugins.appcenter.task.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import io.jenkins.plugins.appcenter.AppCenterException;
import io.jenkins.plugins.appcenter.AppCenterLogger;
import io.jenkins.plugins.appcenter.api.AppCenterServiceFactory;
import io.jenkins.plugins.appcenter.task.request.UploadRequest;
import io.jenkins.plugins.appcenter.util.RemoteFileUtils;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

@Singleton
public final class UploadAppToResourceTask implements AppCenterTask<UploadRequest>, AppCenterLogger {

    private static final long serialVersionUID = 1L;
    private static final int MAX_NON_CHUNKED_UPLOAD_SIZE = (1024 * 1024) * 256; // 256 MB in bytes

    @NonNull
    private final TaskListener taskListener;
    @NonNull
    private final AppCenterServiceFactory factory;
    @NonNull
    private final RemoteFileUtils remoteFileUtils;

    @Inject
    UploadAppToResourceTask(@NonNull final TaskListener taskListener,
                            @NonNull final AppCenterServiceFactory factory,
                            @NonNull final RemoteFileUtils remoteFileUtils) {
        this.taskListener = taskListener;
        this.factory = factory;
        this.remoteFileUtils = remoteFileUtils;
    }

    @NonNull
    @Override
    public CompletableFuture<UploadRequest> execute(@NonNull UploadRequest request) {
        if (request.symbolUploadUrl == null) {
            return uploadApp(request);
        } else {
            return uploadApp(request)
                .thenCompose(this::uploadSymbols);
        }
    }

    @NonNull
    private CompletableFuture<UploadRequest> uploadApp(@NonNull UploadRequest request) {
        requireNonNull(request.uploadDomain, "uploadDomain cannot be null");
        requireNonNull(request.packageAssetId, "packageAssetId cannot be null");
        requireNonNull(request.token, "token cannot be null");
        final Integer chunkSize = requireNonNull(request.chunkSize, "chunkSize cannot be null");

        log("Uploading app to resource.");

        final CompletableFuture<UploadRequest> future = new CompletableFuture<>();

        int offset = 0;
        int blockNumber = 1;
        calculateChunks(request, offset, chunkSize, blockNumber, future);

        return future;
    }

    private void calculateChunks(@NonNull UploadRequest request, int offset, int chunkSize, int blockNumber, @NonNull CompletableFuture<UploadRequest> future) {
        // TODO: Retrofit (via OkHttp) is supposed to be able to do this natively if you set the contentLength to -1. Investigate
        final String url = getUrl(request);
        final File file = remoteFileUtils.getRemoteFile(request.pathToApp);
        final long fileSize = file.length();
        final int noOfBlocks = (int) Math.ceil((double) fileSize / chunkSize);

        try (final BufferedSource bufferedSource = Okio.buffer(Okio.source(file))) {
            bufferedSource.skip(offset);
            final Buffer buffer = new Buffer();
            bufferedSource.request(chunkSize);
            bufferedSource.read(buffer, chunkSize);
            final RequestBody requestFile = RequestBody.create(buffer.readByteArray(), null);
            upload(request, offset, chunkSize, url, requestFile, blockNumber, noOfBlocks, future);
        } catch (IOException e) {
            final AppCenterException exception = logFailure("Upload app to resource unsuccessful", e);
            future.completeExceptionally(exception);
        }
    }

    private void upload(@NonNull UploadRequest request, int offset, int chunkSize, @NonNull String url, @NonNull RequestBody requestFile, int blockNumber, int noOfBlocks, @NonNull CompletableFuture<UploadRequest> future) {
        factory.createAppCenterService()
            .uploadApp(url + "&block_number=" + blockNumber, requestFile)
            .whenComplete((responseBody, throwable) -> {
                if (throwable != null) {
                    final AppCenterException exception = logFailure("Upload app to resource unsuccessful", throwable);
                    future.completeExceptionally(exception);
                } else {
                    log(String.format("Upload app to resource chunk %1$d / %2$d successful.", blockNumber, noOfBlocks));
                    if (blockNumber == noOfBlocks) {
                        future.complete(request);
                    } else {
                        calculateChunks(request, offset + chunkSize, chunkSize, blockNumber + 1, future);
                    }

                }
            });
    }

    @NonNull
    private String getUrl(@NonNull UploadRequest request) {
        return String.format("%1$s/upload/upload_chunk/%2$s?token=%3$s", request.uploadDomain, request.packageAssetId, request.token);
    }

    @NonNull
    private CompletableFuture<UploadRequest> uploadSymbols(@NonNull UploadRequest request) {
        final String pathToDebugSymbols = request.pathToDebugSymbols;
        final String symbolUploadUrl = requireNonNull(request.symbolUploadUrl, "symbolUploadUrl cannot be null");

        final File file = remoteFileUtils.getRemoteFile(pathToDebugSymbols);
        if (file.length() > MAX_NON_CHUNKED_UPLOAD_SIZE) {
            return uploadSymbolsChunked(request, symbolUploadUrl, file);
        } else {
            return uploadSymbolsComplete(request, symbolUploadUrl, file);
        }
    }

    @NonNull
    private CompletableFuture<UploadRequest> uploadSymbolsChunked(@NonNull UploadRequest request, @NonNull String symbolUploadUrl, @NonNull File file) {
        log("Uploading symbols to resource chunked.");

        final CompletableFuture<UploadRequest> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> factory.createBlobUploadService(symbolUploadUrl).uploadFromFile(file.getPath(), true))
            .whenComplete((responseBody, throwable) -> {
                if (throwable != null) {
                    final AppCenterException exception = logFailure("Upload symbols to resource chunked unsuccessful: ", throwable);
                    future.completeExceptionally(exception);
                } else {
                    log("Upload symbols to resource chunked successful.");
                    future.complete(request);
                }
            });

        return future;
    }

    @NonNull
    private CompletableFuture<UploadRequest> uploadSymbolsComplete(@NonNull UploadRequest request, @NonNull String symbolUploadUrl, @NonNull File file) {
        log("Uploading all symbols at once to resource.");

        final CompletableFuture<UploadRequest> future = new CompletableFuture<>();
        final RequestBody requestFile = RequestBody.create(file, null);

        factory.createUploadService(symbolUploadUrl)
            .uploadSymbols(symbolUploadUrl, requestFile)
            .whenComplete((responseBody, throwable) -> {
                if (throwable != null) {
                    final AppCenterException exception = logFailure("Upload symbols to resource unsuccessful: ", throwable);
                    future.completeExceptionally(exception);
                } else {
                    log("Upload symbols to resource successful.");
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