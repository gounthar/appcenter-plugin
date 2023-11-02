package io.jenkins.plugins.appcenter.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.appcenter.model.appcenter.*;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.*;

import java.util.concurrent.CompletableFuture;

public interface AppCenterService {

    @POST("v0.1/apps/{owner_name}/{app_name}/uploads/releases")
    CompletableFuture<ReleaseUploadBeginResponse> releaseUploadsCreate(
        @Path("owner_name") @NonNull String user,
        @Path("app_name") @NonNull String appName,
        @Body @NonNull ReleaseUploadBeginRequest releaseUploadBeginRequest);

    @POST
    CompletableFuture<SetMetadataResponse> setMetaData(@Url @NonNull String url);

    @Headers("Content-Type: application/octet-stream")
    @POST
    CompletableFuture<ResponseBody> uploadApp(@Url @NonNull String url, @Body @NonNull RequestBody file);

    @POST
    CompletableFuture<ResponseBody> finishRelease(@Url @NonNull String url);

    @PATCH("v0.1/apps/{owner_name}/{app_name}/uploads/releases/{upload_id}")
    CompletableFuture<UpdateReleaseUploadResponse> updateReleaseUpload(
        @Path("owner_name") @NonNull String user,
        @Path("app_name") @NonNull String appName,
        @Path("upload_id") @NonNull String uploadId,
        @Body @NonNull UpdateReleaseUploadRequest updateReleaseUploadRequest);

    @GET("v0.1/apps/{owner_name}/{app_name}/uploads/releases/{upload_id}")
    CompletableFuture<PollForReleaseResponse> pollForRelease(
        @Path("owner_name") @NonNull String user,
        @Path("app_name") @NonNull String appName,
        @Path("upload_id") @NonNull String uploadId);

    @PATCH("v0.1/apps/{owner_name}/{app_name}/release_uploads/{upload_id}")
    CompletableFuture<ReleaseUploadEndResponse> releaseUploadsComplete(
        @Path("owner_name") @NonNull String user,
        @Path("app_name") @NonNull String appName,
        @Path("upload_id") @NonNull String uploadId,
        @Body @NonNull ReleaseUploadEndRequest releaseUploadEndRequest);

    @PATCH("v0.1/apps/{owner_name}/{app_name}/releases/{release_id}")
    CompletableFuture<ReleaseDetailsUpdateResponse> releasesUpdate(
        @Path("owner_name") @NonNull String user,
        @Path("app_name") @NonNull String appName,
        @Path("release_id") @NonNull Integer releaseId,
        @Body @NonNull ReleaseUpdateRequest releaseUpdateRequest);

    @POST("v0.1/apps/{owner_name}/{app_name}/symbol_uploads")
    CompletableFuture<SymbolUploadBeginResponse> symbolUploadsCreate(
        @Path("owner_name") @NonNull String user,
        @Path("app_name") @NonNull String appName,
        @Body @NonNull SymbolUploadBeginRequest symbolUploadBeginRequest);

    @PATCH("v0.1/apps/{owner_name}/{app_name}/symbol_uploads/{symbol_upload_id}")
    CompletableFuture<SymbolUpload> symbolUploadsComplete(
        @Path("owner_name") @NonNull String user,
        @Path("app_name") @NonNull String appName,
        @Path("symbol_upload_id") @NonNull String uploadId,
        @Body @NonNull SymbolUploadEndRequest symbolUploadEndRequest);
}