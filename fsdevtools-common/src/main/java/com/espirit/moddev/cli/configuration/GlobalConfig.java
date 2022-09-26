/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.configuration;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.extsync.SyncDirectoryFactory;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.io.FileSystemsAgent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A configuration class for a common configuration in a FirstSpirit environment.
 * Some getters return the value of a corresponding environment variable, if the provided
 * value is empty or no value is provided at all.
 *
 * @author e-Spirit GmbH
 */
public class GlobalConfig implements Config {

	/**
	 * {@link de.espirit.firstspirit.access.project.ProjectScriptContext} used by this configuration.
	 */
	protected ProjectScriptContext _context;

	/**
	 * Boolean flag that indicates if the synchronization directory should be created if it does not exist.
	 */
	@Option(type = OptionType.GLOBAL, name = {"--dont-create-sync-dir"}, description = "Do not create synchronisation directory if it is missing")
	protected boolean dontCreateSynchronizationDirectoryIfMissing;

	private final Environment _environment = new Environment();

	@Option(type = OptionType.GLOBAL, name = "-e", description = "Error mode. Shows error stacktraces.", title = "showStacktraces")
	private boolean _error;

	@Option(type = OptionType.GLOBAL, name = {"-h", "--host"}, description = "FirstSpirit host. Default is localhost.", title = "host")
	private String _host;

	@Option(type = OptionType.GLOBAL, name = {"-c", "--conn-mode"}, description = "FirstSpirit connection mode. Default is HTTP.", title = "mode")
	@AllowedRawValues(allowedValues = {"HTTP", "HTTPS", "SOCKET"})
	private FsConnectionType _fsMode;

	@Option(type = OptionType.GLOBAL, name = {"-port"}, description = "FirstSpirit host's port. Default is 8000.", title = "port")
	private Integer _port;

	@Option(type = OptionType.GLOBAL, name = {"-sz", "--servletzone"}, description = "The FirstSpirit servlet zone. Default is /.", title = "servletzone")
	private String _servletZone = CliConstants.DEFAULT_SERVLET_ZONE.value();

	@Option(type = OptionType.GLOBAL, name = {"-hph", "--httpproxyhost"}, description = "Proxy host for HTTP/HTTPS connections.", title = "proxyHost")
	private String _httpProxyHost;

	@Option(type = OptionType.GLOBAL, name = {"-hpp", "--httpproxyport"}, description = "Proxy host's port for HTTP/HTTPS connections. Default is 8080.", title = "proxyPort")
	private Integer _httpProxyPort;

	@Option(type = OptionType.GLOBAL, name = {"-u", "--user"}, description = "FirstSpirit user. Default is Admin.", title = "userName")
	private String _user;

	@Option(type = OptionType.GLOBAL, name = {"-pwd", "--password"}, description = "FirstSpirit user's password. Default is Admin.", title = "password")
	private String _password;

	@Option(type = OptionType.GLOBAL, name = {"-p", "--project"}, description = "Name of FirstSpirit project", title = "projectName")
	private String _project;

	@Option(type = OptionType.GLOBAL, name = {"-a", "--activateProjectIfDeactivated"}, description = "Activates a project if deactivated for any reason", title = "forceActivation")
	private boolean _activateProjectIfDeactivated;

	@Option(type = OptionType.GLOBAL, name = {"-sd", "--syncDir"}, description = "The synchronization directory that is used for im- and export. Default is current directory", title = "syncDirectory")
	private String _synchronizationDirectory = ".";

	@Option(type = OptionType.GLOBAL, name = {"-rf", "--resultFile"}, description = "The path of the JSON-Result. Default is '" + FsUtil.VALUE_DEFAULT_RESULT_FILE + "'.", title = "resultFile")
	private String _resultFile;

	public GlobalConfig() {
	}

	@Override
	public final void setContext(ProjectScriptContext context) {
		if (context == null) {
			throw new IllegalArgumentException("Context should not be null");
		}
		_context = context;
	}

	/**
	 * Get the {@link de.espirit.firstspirit.access.project.ProjectScriptContext} used by this configuration.
	 *
	 * @return the context used by this configuration
	 */
	public ProjectScriptContext getContext() {
		return _context;
	}

	/**
	 * Indicates if the error mode is enabled.
	 * If in error mode, the cli application will log the full stack trace of an error.
	 *
	 * @return true if error mode is enabled, otherwise false
	 */
	public boolean isError() {
		return _error;
	}

	/**
	 * Get the {@link Environment} used by this instance.
	 *
	 * @return the {@link Environment} used by this instance
	 */
	public Environment getEnvironment() {
		return _environment;
	}

	@Override
	public String getHttpProxyHost() {
		if (_httpProxyHost == null || _httpProxyHost.isEmpty()) {
			boolean environmentContainsHost = getEnvironment().containsKey(CliConstants.KEY_FS_HTTP_PROXYHOST.value());
			if (environmentContainsHost) {
				return getEnvironment().get(CliConstants.KEY_FS_HTTP_PROXYHOST.value()).trim();
			}
			return "";
		}
		return _httpProxyHost;
	}

	@Override
	public Integer getHttpProxyPort() {
		if (_httpProxyPort == null) {
			boolean environmentContainsPort = getEnvironment().containsKey(CliConstants.KEY_FS_HTTP_PROXYPORT.value());
			if (environmentContainsPort) {
				return Integer.valueOf(getEnvironment().get(CliConstants.KEY_FS_HTTP_PROXYPORT.value()).trim());
			}
			return 8080;
		}
		return _httpProxyPort;
	}

	@Override
	public String getHost() {
		if (_host == null || _host.isEmpty()) {
			boolean environmentContainsHost = getEnvironment().containsKey(CliConstants.KEY_FS_HOST.value());
			if (environmentContainsHost) {
				return getEnvironment().get(CliConstants.KEY_FS_HOST.value()).trim();
			}
			return CliConstants.DEFAULT_HOST.value();
		}
		return _host;
	}

	@Override
	public Integer getPort() {
		if (_port == null) {
			boolean environmentContainsPort = getEnvironment().containsKey(CliConstants.KEY_FS_PORT.value());
			if (environmentContainsPort) {
				return Integer.valueOf(getEnvironment().get(CliConstants.KEY_FS_PORT.value()).trim());
			}
			return getConnectionMode().getDefaultPort();
		}
		return _port;
	}

	@Override
	public String getServletZone() {
		if (_servletZone == null) {
			boolean environmentContainsServletZone = getEnvironment().containsKey(CliConstants.KEY_FS_SERVLETZONE.value());
			if (environmentContainsServletZone) {
				return getEnvironment().get(CliConstants.KEY_FS_SERVLETZONE.value()).trim();
			}
		}
		return _servletZone;
	}

	@Override
	public FsConnectionType getConnectionMode() {
		if (_fsMode == null) {
			boolean environmentContainsPort = getEnvironment().containsKey(CliConstants.KEY_FS_MODE.value());
			if (environmentContainsPort) {
				return FsConnectionType.valueOf(getEnvironment().get(CliConstants.KEY_FS_MODE.value()).trim().toUpperCase(Locale.UK));
			}
			return FsConnectionType.valueOf(CliConstants.DEFAULT_CONNECTION_MODE.value());
		}
		return _fsMode;
	}

	@Override
	public String getUser() {
		if (_user == null || _user.isEmpty()) {
			boolean environmentContainsUser = getEnvironment().containsKey(CliConstants.KEY_FS_USER.value());
			if (environmentContainsUser) {
				return getEnvironment().get(CliConstants.KEY_FS_USER.value()).trim();
			}
			return CliConstants.DEFAULT_USER.value();
		}
		return _user;
	}

	@Override
	public String getPassword() {
		if (_password == null || _password.isEmpty()) {
			boolean environmentContainsPassword = getEnvironment().containsKey(CliConstants.KEY_FS_PASSWORD.value());
			if (environmentContainsPassword) {
				return getEnvironment().get(CliConstants.KEY_FS_PASSWORD.value()).trim();
			}
			return CliConstants.DEFAULT_USER.value();
		}
		return _password;
	}

	@Override
	@NotNull
	public String getResultFile() {
		if (_resultFile == null) {
			boolean environmentContainsResultFile = getEnvironment().containsKey(CliConstants.KEY_RESULT_FILE.value());
			if (environmentContainsResultFile) {
				return getEnvironment().get(CliConstants.KEY_RESULT_FILE.value()).trim();
			}
		}
		return _resultFile != null ? _resultFile : FsUtil.VALUE_DEFAULT_RESULT_FILE;
	}

	@Override
	public String getProject() {
		if (_project == null || _project.isEmpty()) {
			boolean environmentContainsProject = getEnvironment().containsKey(CliConstants.KEY_FS_PROJECT.value());
			if (environmentContainsProject) {
				return getEnvironment().get(CliConstants.KEY_FS_PROJECT.value()).trim();
			}
		}
		return _project;
	}

	@Override
	public boolean isActivateProjectIfDeactivated() {
		return _activateProjectIfDeactivated;
	}

	@Override
	public String getSynchronizationDirectoryString() {
		return _synchronizationDirectory;
	}

	@Override
	public <F extends FileHandle> FileSystem<F> getSynchronizationDirectory() {
		return getSynchronizationDirectory(getSynchronizationDirectoryString());
	}

	protected <F extends FileHandle> FileSystem<F> getSynchronizationDirectory(final String syncDirStr) {
		SyncDirectoryFactory syncDirectoryFactory = new SyncDirectoryFactory(this);
		syncDirectoryFactory.checkAndCreateSyncDirIfNeeded(syncDirStr);

		final FileSystemsAgent fileSystemsAgent = _context.requireSpecialist(FileSystemsAgent.TYPE);
		return (FileSystem<F>) fileSystemsAgent.getOSFileSystem(syncDirStr);
	}

	@Override
	public boolean createSynchronizationDirectoryIfMissing() {
		return !dontCreateSynchronizationDirectoryIfMissing;
	}

	/**
	 * Enable or disable the error mode.
	 * If the error mode is enabled, the full stack trace of errors will be logged.
	 *
	 * @param error boolean value indicating if the error code should be enabled or not
	 */
	public void setError(boolean error) {
		_error = error;
	}

	/**
	 * Set the FirstSpirit server host that the cli application will connect to.
	 *
	 * @param host FirstSpirit server host
	 */
	public void setHost(String host) {
		_host = host;
	}

	/**
	 * Set the FirstSpirit server servlet zone.
	 *
	 * @param servletZone FirstSpirit servlet zone
	 */
	public void setServletZone(String servletZone) {
		_servletZone = servletZone;
	}

	/**
	 * Get the {@link FsConnectionType} used to connect to FirstSpirit.
	 *
	 * @return the {@link FsConnectionType} used to connect to FirstSpirit
	 */
	public FsConnectionType getFsMode() {
		return _fsMode;
	}

	/**
	 * Set the {@link FsConnectionType} used to connect to FirstSpirit.
	 *
	 * @param fsMode the {@link FsConnectionType} used to connect to FirstSpirit
	 */
	public void setFsMode(FsConnectionType fsMode) {
		_fsMode = fsMode;
	}

	/**
	 * Set the FirstSpirit server port that the cli application will connect to.
	 *
	 * @param port FirstSpirit server port
	 */
	public void setPort(Integer port) {
		_port = port;
	}

	/**
	 * Set the HTTP/HTTPS proxy host that the cli will use for the connection.
	 *
	 * @param host HTTP/HTTPS proxy host
	 */
	public void setHttpProxyHost(final String host) {
		_httpProxyHost = host;
	}

	/**
	 * Set the HTTP/HTTPS proxy port that the cli will use for the connection.
	 *
	 * @param port HTTP/HTTPS proxy port
	 */
	public void setHttpProxyPort(Integer port) {
		_httpProxyPort = port;
	}

	/**
	 * Set the user used to authenticate against FirstSpirit.
	 *
	 * @param user the username used to authenticate against FirstSpirit
	 */
	public void setUser(String user) {
		_user = user;
	}

	/**
	 * Set the password used to authenticate against FirstSpirit.
	 *
	 * @param password the password used to authenticate against FirstSpirit
	 */
	public void setPassword(String password) {
		_password = password;
	}

	/**
	 * Set the name of the project that will be synchronized.
	 *
	 * @param project the name of the project that will be synchronized
	 */
	public void setProject(String project) {
		_project = project;
	}

	/**
	 * Enable or disable the automatic activation of the synchronized project.
	 *
	 * @param activateProjectIfDeactivated a boolean value indicating if the automatic activation of the synchronized project should be enabled or not
	 */
	public void setActivateProjectIfDeactivated(boolean activateProjectIfDeactivated) {
		_activateProjectIfDeactivated = activateProjectIfDeactivated;
	}

	/**
	 * Set the synchronization directory.
	 *
	 * @param synchronizationDirectory Path to the synchronization directory
	 */
	public void setSynchronizationDirectory(String synchronizationDirectory) {
		_synchronizationDirectory = synchronizationDirectory;
	}

}
