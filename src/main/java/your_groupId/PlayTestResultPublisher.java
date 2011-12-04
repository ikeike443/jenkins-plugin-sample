package your_groupId;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author ikeike443
 */
public class PlayTestResultPublisher extends Recorder {
    @DataBoundConstructor
    public PlayTestResultPublisher() {
    }


    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        InputStream inputStream = null;
        try {

            FilePath[] files = build.getWorkspace().list("test-result/*");
            FilePath root = new FilePath(build.getRootDir());
            for (FilePath filePath : files) {
                filePath.copyTo(new FilePath(root, "test-result/" + filePath.getName()));
            }
            Properties conf = new Properties();
            inputStream = new FileInputStream(new File(
                    build.getWorkspace() + "/conf/application.conf"));
            conf.load(inputStream);

            PlayTestResultAction act = new PlayTestResultAction(build);
            act.setPassed(new FilePath(root, "test-result/result.passed").exists());
            act.setAppName(conf.getProperty("application.name"));
            build.addAction(act);


            return true;

        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return false;
        } finally {
            try {inputStream.close();} catch (Exception ignore) {}
        }

    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link PlayTestResultPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>views/hudson/plugins/hello_world/your_groupId/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // this marker indicates Hudson that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }


        @Override
        public String getDisplayName() {
            return "Play! auto-test reports";
        }

    }

}

