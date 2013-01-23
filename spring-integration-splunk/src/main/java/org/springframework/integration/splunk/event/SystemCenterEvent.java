/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.splunk.event;

/**
 * @author David Turanski
 *
 */
@SuppressWarnings("serial")
public class SystemCenterEvent extends SplunkEvent {
	// ----------------------------------
	// System center
	// ----------------------------------

	/**
	 * The running application or service on the system (the src field), such as
	 * explorer.exe or sshd.
	 */
	public static String SYSTEM_CENTER_APP = "app";
	/**
	 * The amount of disk space available per drive or mount (the mount field)
	 * on the system (the src field).
	 */
	public static String SYSTEM_CENTER_FREEMBYTES = "FreeMBytes";
	/**
	 * The version of operating system installed on the host (the src field),
	 * such as 6.0.1.4 or 2.6.27.30-170.2.82.fc10.x86_64.
	 */
	public static String SYSTEM_CENTER_KERNEL_RELEASE = "kernel_release";
	/**
	 * Human-readable version of the SystemUptime value.
	 */
	public static String SYSTEM_CENTER_LABEL = "label";
	/**
	 * The drive or mount reporting available disk space (the FreeMBytes field)
	 * on the system (the src field).
	 */
	public static String SYSTEM_CENTER_MOUNT = "mount";
	/**
	 * The name of the operating system installed on the host (the src), such as
	 * Microsoft Windows Server 2003 or GNU/Linux).
	 */
	public static String SYSTEM_CENTER_OS = "os";
	/**
	 * The percentage of processor utilization.
	 */
	public static String SYSTEM_CENTER_PERCENTPROCESSORTIME = "PercentProcessorTime";
	/**
	 * The setlocaldefs setting from the SE Linux configuration.
	 */
	public static String SYSTEM_CENTER_SETLOCALDEFS = "setlocaldefs";
	/**
	 * Values from the SE Linux configuration file.
	 */
	public static String SYSTEM_CENTER_SELINUX = "selinux";
	/**
	 * The SE Linux type (such as targeted).
	 */
	public static String SYSTEM_CENTER_SELINUXTYPE = "selinuxtype";
	/**
	 * The shell provided to the User Account (the user field) upon logging into
	 * the system (the src field).
	 */
	public static String SYSTEM_CENTER_SHELL = "shell";
	/**
	 * The TCP/UDP source port on the system (the src field).
	 */
	public static String SYSTEM_CENTER_SRC_PORT = "src_port";
	/**
	 * The sshd protocol version.
	 */
	public static String SYSTEM_CENTER_SSHD_PROTOCOL = "sshd_protocol";
	/**
	 * The start mode of the given service.
	 */
	public static String SYSTEM_CENTER_STARTMODE = "Startmode";
	/**
	 * The number of seconds since the system (the src) has been "up."
	 */
	public static String SYSTEM_CENTER_SYSTEMUPTIME = "SystemUptime";
	/**
	 * The total amount of available memory on the system (the src).
	 */
	public static String SYSTEM_CENTER_TOTALMBYTES = "TotalMBytes";
	/**
	 * The amount of used memory on the system (the src).
	 */
	public static String SYSTEM_CENTER_USEDMBYTES = "UsedMBytes";
	/**
	 * The User Account present on the system (the src).
	 */
	public static String SYSTEM_CENTER_USER = "user";
	/**
	 * The number of updates the system (the src) is missing.
	 */
	public static String SYSTEM_CENTER_UPDATES = "updates";

	public void setSystemCenterApp(String systemCenterApp) {
		addPair(SYSTEM_CENTER_APP, systemCenterApp);
	}

	public void setSystemCenterFreembytes(long systemCenterFreembytes) {
		addPair(SYSTEM_CENTER_FREEMBYTES, systemCenterFreembytes);
	}

	public void setSystemCenterKernelRelease(String systemCenterKernelRelease) {
		addPair(SYSTEM_CENTER_KERNEL_RELEASE, systemCenterKernelRelease);
	}

	public void setSystemCenterLabel(String systemCenterLabel) {
		addPair(SYSTEM_CENTER_LABEL, systemCenterLabel);
	}

	public void setSystemCenterMount(String systemCenterMount) {
		addPair(SYSTEM_CENTER_MOUNT, systemCenterMount);
	}

	public void setSystemCenterOs(String systemCenterOs) {
		addPair(SYSTEM_CENTER_OS, systemCenterOs);
	}

	public void setSystemCenterPercentprocessortime(int systemCenterPercentprocessortime) {
		addPair(SYSTEM_CENTER_PERCENTPROCESSORTIME, systemCenterPercentprocessortime);
	}

	public void setSystemCenterSetlocaldefs(int systemCenterSetlocaldefs) {
		addPair(SYSTEM_CENTER_SETLOCALDEFS, systemCenterSetlocaldefs);
	}

	public void setSystemCenterSelinux(String systemCenterSelinux) {
		addPair(SYSTEM_CENTER_SELINUX, systemCenterSelinux);
	}

	public void setSystemCenterSelinuxtype(String systemCenterSelinuxtype) {
		addPair(SYSTEM_CENTER_SELINUXTYPE, systemCenterSelinuxtype);
	}

	public void setSystemCenterShell(String systemCenterShell) {
		addPair(SYSTEM_CENTER_SHELL, systemCenterShell);
	}

	public void setSystemCenterSrcPort(int systemCenterSrcPort) {
		addPair(SYSTEM_CENTER_SRC_PORT, systemCenterSrcPort);
	}

	public void setSystemCenterSshdProtocol(String systemCenterSshdProtocol) {
		addPair(SYSTEM_CENTER_SSHD_PROTOCOL, systemCenterSshdProtocol);
	}

	public void setSystemCenterStartmode(String systemCenterStartmode) {
		addPair(SYSTEM_CENTER_STARTMODE, systemCenterStartmode);
	}

	public void setSystemCenterSystemuptime(long systemCenterSystemuptime) {
		addPair(SYSTEM_CENTER_SYSTEMUPTIME, systemCenterSystemuptime);
	}

	public void setSystemCenterTotalmbytes(long systemCenterTotalmbytes) {
		addPair(SYSTEM_CENTER_TOTALMBYTES, systemCenterTotalmbytes);
	}

	public void setSystemCenterUsedmbytes(long systemCenterUsedmbytes) {
		addPair(SYSTEM_CENTER_USEDMBYTES, systemCenterUsedmbytes);
	}

	public void setSystemCenterUser(String systemCenterUser) {
		addPair(SYSTEM_CENTER_USER, systemCenterUser);
	}

	public void setSystemCenterUpdates(long systemCenterUpdates) {
		addPair(SYSTEM_CENTER_UPDATES, systemCenterUpdates);
	}

}
