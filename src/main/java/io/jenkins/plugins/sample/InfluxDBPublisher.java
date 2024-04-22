package io.jenkins.plugins.sample;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import javax.servlet.ServletException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import java.util.Random;

public class InfluxDBPublisher extends Recorder {

    private static final Random random = new Random();

    private final String name;
    private String influxdbUri;
    private String adminToken;
    private String organizationId;
    private String bucketName;

    @DataBoundConstructor
    public InfluxDBPublisher(String name) {
        this.name = name;
    }

    @DataBoundSetter
    public void setInfluxdbUri(String influxdbUri) {
        this.influxdbUri = influxdbUri;
    }

    @DataBoundSetter
    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }

    @DataBoundSetter
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    @DataBoundSetter
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getName() {
        return name;
    }

    public String getInfluxdbUri() {
        return influxdbUri;
    }

    public String getAdminToken() {
        return adminToken;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public static double getRandomBuildDuration() {
        // Generate a double between 0.0 (inclusive) and 7.0 (exclusive)
        double duration = 7 * random.nextDouble(); 

        // Shift the range to 8.0 to 15.0
        duration += 8;

        // Round to two decimal places
        return Math.round(duration * 100.0) / 100.0;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        // Get job name
        String jobName = build.getProject().getName();

        // Get build number
        int buildNumber = build.getNumber();

        // Get build status
        String buildStatus = build.getResult().equals(Result.SUCCESS) ? "success" : "failed";

        long durationMillis = build.getDuration();
        // Get build time
        long buildTimeMillis = build.getStartTimeInMillis();
        String buildTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(buildTimeMillis);

        InfluxDBClient influxDBClient =
                InfluxDBClientFactory.create(influxdbUri, adminToken.toCharArray(), organizationId, bucketName);

        listener.getLogger()
                .println("Inserting jobName: " + jobName + ", buildNumber: " + buildNumber + ", buildStatus: "
                        + buildStatus + ", buildTime: " + buildTime + ", durationMillis: " + durationMillis);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        Point point = Point.measurement("job_results")
                .addTag("job_name", jobName)
                .addTag("build_status", buildStatus)
                .addTag("build_duration", Double.toString(getRandomBuildDuration()))
                .addTag("build_number", Integer.toString(buildNumber))
                .addField("build_time", buildTime)
                .time(Instant.now().toEpochMilli(), WritePrecision.MS);

        writeApi.writePoint(point);

        // Close the InfluxDB connection
        influxDBClient.close();

        listener.getLogger().println("Data sent to InfluxDB successfully.");

        return true;
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return new DescriptorImpl();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return "InfluxDB Job Publisher";
        }

        public FormValidation doCheckName() throws IOException, ServletException {
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
    }
}
