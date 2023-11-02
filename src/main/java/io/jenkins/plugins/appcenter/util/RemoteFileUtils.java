package io.jenkins.plugins.appcenter.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;

public class RemoteFileUtils implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final FilePath filePath;

    @Nullable
    private File file;

    @Inject
    RemoteFileUtils(@NonNull final FilePath filePath) {
        this.filePath = filePath;
    }

    @NonNull
    public File getRemoteFile(@NonNull String pathToRemoteFile) {
        if (file == null) {
            file = new File(filePath.child(pathToRemoteFile).getRemote());
        }

        return file;
    }

    @NonNull
    public String getFileName(@NonNull String pathToRemoveFile) {
        return getRemoteFile(pathToRemoveFile).getName();
    }

    public long getFileSize(@NonNull String pathToRemoveFile) {
        return getRemoteFile(pathToRemoveFile).length();
    }

    @NonNull
    public String getContentType(@NonNull String pathToApp) {
        if (pathToApp.endsWith(".apk") || pathToApp.endsWith(".aab")) return "application/vnd.android.package-archive";
        if (pathToApp.endsWith(".msi")) return "application/x-msi";
        if (pathToApp.endsWith(".plist")) return "application/xml";
        if (pathToApp.endsWith(".aetx")) return "application/c-x509-ca-cert";
        if (pathToApp.endsWith(".cer")) return "application/pkix-cert";
        if (pathToApp.endsWith("xap")) return "application/x-silverlight-app";
        if (pathToApp.endsWith(".appx")) return "application/x-appx";
        if (pathToApp.endsWith(".appxbundle")) return "application/x-appxbundle";
        if (pathToApp.endsWith(".appxupload") || pathToApp.endsWith(".appxsym")) return "application/x-appxupload";
        if (pathToApp.endsWith(".msix")) return "application/x-msix";
        if (pathToApp.endsWith(".msixbundle")) return "application/x-msixbundle";
        if (pathToApp.endsWith(".msixupload") || pathToApp.endsWith(".msixsym")) return "application/x-msixupload";

        // Otherwise
        return "application/octet-stream";
    }
}