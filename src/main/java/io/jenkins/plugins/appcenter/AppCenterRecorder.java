package io.jenkins.plugins.appcenter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.plugins.appcenter.di.AppCenterComponent;
import io.jenkins.plugins.appcenter.di.DaggerAppCenterComponent;
import io.jenkins.plugins.appcenter.task.UploadTask;
import io.jenkins.plugins.appcenter.validator.ApiTokenValidator;
import io.jenkins.plugins.appcenter.validator.AppNameValidator;
import io.jenkins.plugins.appcenter.validator.BuildVersionValidator;
import io.jenkins.plugins.appcenter.validator.DistributionGroupsValidator;
import io.jenkins.plugins.appcenter.validator.PathPlaceholderValidator;
import io.jenkins.plugins.appcenter.validator.PathToAppValidator;
import io.jenkins.plugins.appcenter.validator.PathToDebugSymbolsValidator;
import io.jenkins.plugins.appcenter.validator.PathToReleaseNotesValidator;
import io.jenkins.plugins.appcenter.validator.UsernameValidator;
import io.jenkins.plugins.appcenter.validator.Validator;
import io.jenkins.plugins.appcenter.validator.CommitHashValidator;
import io.jenkins.plugins.appcenter.validator.BranchNameValidator;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.IOException;
import java.io.PrintStream;

import static hudson.model.Result.FAILURE;
import static hudson.model.Result.SUCCESS;

@SuppressWarnings("unused")
public final class AppCenterRecorder extends Recorder implements SimpleBuildStep {

    @NonNull
    private final Secret apiToken;

    @NonNull
    private final String ownerName;

    @NonNull
    private final String appName;

    @NonNull
    private final String pathToApp;

    @NonNull
    private final String distributionGroups;

    @Nullable
    private String releaseNotes;

    private boolean notifyTesters = true;

    private boolean mandatoryUpdate = false;

    @Nullable
    private String buildVersion;

    @Nullable
    private String branchName;

    @Nullable
    private String commitHash;

    @Nullable
    private String pathToDebugSymbols;

    @Nullable
    private String pathToReleaseNotes;

    @Nullable
    private transient String baseUrl;

    @DataBoundConstructor
    public AppCenterRecorder(@Nullable String apiToken, @Nullable String ownerName, @Nullable String appName, @Nullable String pathToApp, @Nullable String distributionGroups) {
        this.apiToken = Secret.fromString(apiToken);
        this.ownerName = Util.fixNull(ownerName);
        this.appName = Util.fixNull(appName);
        this.pathToApp = Util.fixNull(pathToApp);
        this.distributionGroups = Util.fixNull(distributionGroups);
    }

    @NonNull
    public Secret getApiToken() {
        return apiToken;
    }

    @NonNull
    public String getOwnerName() {
        return ownerName;
    }

    @NonNull
    public String getAppName() {
        return appName;
    }

    @NonNull
    public String getPathToApp() {
        return pathToApp;
    }

    @NonNull
    public String getDistributionGroups() {
        return distributionGroups;
    }

    @NonNull
    public String getReleaseNotes() {
        return Util.fixNull(releaseNotes);
    }

    public boolean getNotifyTesters() {
        return notifyTesters;
    }

    public boolean getMandatoryUpdate() {
        return mandatoryUpdate;
    }

    @NonNull
    public String getBuildVersion() {
        return Util.fixNull(buildVersion);
    }

    @NonNull
    public String getPathToDebugSymbols() {
        return Util.fixNull(pathToDebugSymbols);
    }

    @NonNull
    public String getPathToReleaseNotes() {
        return Util.fixNull(pathToReleaseNotes);
    }

    @NonNull
    public String getBranchName() {
        return Util.fixNull(branchName);
    }

    @NonNull
    public String getCommitHash() {
        return Util.fixNull(commitHash);
    }

    @DataBoundSetter
    public void setReleaseNotes(@Nullable String releaseNotes) {
        this.releaseNotes = Util.fixEmpty(releaseNotes);
    }

    @DataBoundSetter
    public void setNotifyTesters(boolean notifyTesters) {
        this.notifyTesters = notifyTesters;
    }

    @DataBoundSetter
    public void setMandatoryUpdate(boolean mandatoryUpdate) {
        this.mandatoryUpdate = mandatoryUpdate;
    }

    @DataBoundSetter
    public void setBuildVersion(@Nullable String buildVersion) { this.buildVersion = Util.fixEmpty(buildVersion); }

    @DataBoundSetter
    public void setBranchName(@Nullable String branchName) { this.branchName = Util.fixEmpty(branchName); }

    @DataBoundSetter
    public void setCommitHash(@Nullable String commitHash) { this.commitHash = Util.fixEmpty(commitHash); }

    @DataBoundSetter
    public void setPathToDebugSymbols(@Nullable String pathToDebugSymbols) {
        this.pathToDebugSymbols = Util.fixEmpty(pathToDebugSymbols);
    }

    @DataBoundSetter
    public void setPathToReleaseNotes(@Nullable String pathToReleaseNotes) {
        this.pathToReleaseNotes = Util.fixEmpty(pathToReleaseNotes);
    }

    /**
     * Do not use outside of testing.
     *
     * @param baseUrl String Sets a new base url
     */
    public void setBaseUrl(@Nullable String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath filePath, @NonNull Launcher launcher, @NonNull TaskListener taskListener) throws InterruptedException, IOException {
        final Result buildResult = run.getResult();
        if (buildResult != null && buildResult.isWorseOrEqualTo(FAILURE)) {
            taskListener.getLogger().println(Messages.AppCenterRecorder_DescriptorImpl_errors_upstreamBuildFailure());
            return;
        }

        if (uploadToAppCenter(run, filePath, taskListener)) {
            run.setResult(SUCCESS);
        } else {
            run.setResult(FAILURE);
        }
    }

    private boolean uploadToAppCenter(Run<?, ?> run, FilePath filePath, TaskListener taskListener) throws IOException, InterruptedException {
        final AppCenterComponent component = DaggerAppCenterComponent.factory().create(this, Jenkins.get(), run, filePath, taskListener, baseUrl);
        final UploadTask uploadTask = component.uploadTask();
        final PrintStream logger = taskListener.getLogger();

        try {
            return filePath.act(uploadTask);
        } catch (AppCenterException e) {
            e.printStackTrace(logger);
            return false;
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Symbol("appCenter")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @SuppressWarnings("unused")
        public FormValidation doCheckApiToken(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_missingApiToken());
            }

            final Validator validator = new ApiTokenValidator();

            if (!validator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidApiToken());
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckOwnerName(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_missingOwnerName());
            }

            final Validator validator = new UsernameValidator();

            if (!validator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidOwnerName());
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckAppName(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_missingAppName());
            }

            final Validator validator = new AppNameValidator();

            if (!validator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidAppName());
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckPathToApp(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_missingPathToApp());
            }

            final Validator pathToAppValidator = new PathToAppValidator();

            if (!pathToAppValidator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidPath());
            }

            final Validator pathPlaceholderValidator = new PathPlaceholderValidator();

            if (!pathPlaceholderValidator.isValid(value)) {
                return FormValidation.warning(Messages.AppCenterRecorder_DescriptorImpl_warnings_mustNotStartWithEnvVar());
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckDistributionGroups(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_missingDistributionGroups());
            }

            final Validator validator = new DistributionGroupsValidator();

            if (!validator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidDistributionGroups());
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckBuildVersion(@QueryParameter String value) {
            if (value.trim().isEmpty()) {
                return FormValidation.ok();
            }

            final Validator buildVersionValidator = new BuildVersionValidator();

            if (!buildVersionValidator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidBuildVersion());
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckPathToDebugSymbols(@QueryParameter String value) {
            if (value.trim().isEmpty()) {
                return FormValidation.ok();
            }

            final Validator pathToDebugSymbolsValidator = new PathToDebugSymbolsValidator();

            if (!pathToDebugSymbolsValidator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidPath());
            }

            final Validator pathPlaceholderValidator = new PathPlaceholderValidator();

            if (!pathPlaceholderValidator.isValid(value)) {
                return FormValidation.warning(Messages.AppCenterRecorder_DescriptorImpl_warnings_mustNotStartWithEnvVar());
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckPathToReleaseNotes(@QueryParameter String value) {
            if (value.trim().isEmpty()) {
                return FormValidation.ok();
            }

            final Validator pathToReleaseNotesValidator = new PathToReleaseNotesValidator();

            if (!pathToReleaseNotesValidator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidPath());
            }

            final Validator pathPlaceholderValidator = new PathPlaceholderValidator();

            if (!pathPlaceholderValidator.isValid(value)) {
                return FormValidation.warning(Messages.AppCenterRecorder_DescriptorImpl_warnings_mustNotStartWithEnvVar());
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckBranchName(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.ok();
            }

            final Validator validator = new BranchNameValidator();

            if (!validator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidBranchName());
            }

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckCommitHash(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.ok();
            }

            final Validator validator = new CommitHashValidator();

            if (!validator.isValid(value)) {
                return FormValidation.error(Messages.AppCenterRecorder_DescriptorImpl_errors_invalidCommitHash());
            }

            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.AppCenterRecorder_DescriptorImpl_DisplayName();
        }

    }
}