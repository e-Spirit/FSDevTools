/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *********************************************************************
 *
 */

package com.espirit.moddev.cli.commands.service.common;

public class ServiceInfo {
	private ServiceStatus previousStatus;
	private ServiceStatus currentStatus;
	private String serviceName;

	public ServiceInfo(String serviceName, ServiceStatus previousStatus, ServiceStatus currentStatus) {
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

		ServiceInfo that = (ServiceInfo) o;

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
