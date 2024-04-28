package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.ExtensionList;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;

@Extension
public class InfluxDBConfigurationGlobal extends GlobalConfiguration {
    public static InfluxDBConfigurationGlobal get() {
        return ExtensionList.lookupSingleton(InfluxDBConfigurationGlobal.class);
    }

    private String configID;
    private String influxdbUri;
    private String adminToken;
    private String organizationId;
    private String bucketName;

    public InfluxDBConfigurationGlobal() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    public String getConfigID() {
        return configID;
    }

    @DataBoundSetter
    public void setConfigID(String configID) {
        this.configID = configID;
        save();
    }

    public String getInfluxdbUri() {
        return influxdbUri;
    }

    @DataBoundSetter
    public void setInfluxdbUri(String influxdbUri) {
        this.influxdbUri = influxdbUri;
        save();
    }

    public String getAdminToken() {
        return adminToken;
    }

    @DataBoundSetter
    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
        save();
    }

    public String getOrganizationId() {
        return organizationId;
    }

    @DataBoundSetter
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        save();
    }

    public String getBucketName() {
        return bucketName;
    }

    @DataBoundSetter
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
        save();
    }
}
