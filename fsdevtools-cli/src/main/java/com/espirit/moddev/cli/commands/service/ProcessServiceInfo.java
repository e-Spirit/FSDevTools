package com.espirit.moddev.cli.commands.service;

public class ProcessServiceInfo {
    private ServiceStatus previousStatus;
    private ServiceStatus currentStatus;
    private String serviceName;

    public ProcessServiceInfo(String serviceName, ServiceStatus previousStatus, ServiceStatus currentStatus) {
        this.serviceName = serviceName;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
    }

    public ServiceStatus getPreviousStatus() {
        return previousStatus;
    }

    public ServiceStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessServiceInfo that = (ProcessServiceInfo) o;

        if (previousStatus != that.previousStatus) return false;
        if (currentStatus != that.currentStatus) return false;
        return serviceName != null ? serviceName.equals(that.serviceName) : that.serviceName == null;
    }

    @Override
    public int hashCode() {
        int result = previousStatus != null ? previousStatus.hashCode() : 0;
        result = 31 * result + (currentStatus != null ? currentStatus.hashCode() : 0);
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProcessServiceInfo{" +
                "previousStatus=" + previousStatus +
                ", currentStatus=" + currentStatus +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }

    public enum ServiceStatus {
        RUNNING,
        STOPPED
    }
}
