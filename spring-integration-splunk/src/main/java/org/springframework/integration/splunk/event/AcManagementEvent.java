/*
 * Copyright 2011-2012 the original author or authors.
 *
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
 */
package org.springframework.integration.splunk.event;


/**
 * Splunk data entity
 *
 * @author Jarred Li
 * @author Damien Dallimore damien@dtdsoftware.com
 * @author David Turanski
 * @since 1.0
 *
 */
@SuppressWarnings("serial")
public class AcManagementEvent extends SplunkEvent {

	
	/**
	 * Splunk Common Information Model(CIM) Fields
	 */

	// ------------------
	// Account management
	// ------------------

	/**
	 * The domain containing the user that is affected by the account management
	 * event.
	 */
	public static String AC_MANAGEMENT_DEST_NT_DOMAIN = "dest_nt_domain";
	/**
	 * Description of the account management change performed.
	 */
	public static String AC_MANAGEMENT_SIGNATURE = "signature";
	/**
	 * The NT source of the destination. In the case of an account management
	 * event, this is the domain that contains the user that generated the
	 * event.
	 */
	public static String AC_MANAGEMENT_SRC_NT_DOMAIN = "src_nt_domain";

	public void setAcManagementDestNtDomain(String acManagementDestNtDomain) {
		addPair(AC_MANAGEMENT_DEST_NT_DOMAIN, acManagementDestNtDomain);
	}

	public void setAcManagementSignature(String acManagementSignature) {
		addPair(AC_MANAGEMENT_SIGNATURE, acManagementSignature);
	}

	public void setAcManagementSrcNtDomain(String acManagementSrcNtDomain) {
		addPair(AC_MANAGEMENT_SRC_NT_DOMAIN, acManagementSrcNtDomain);
	}
}