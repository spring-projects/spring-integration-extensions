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
public class DnsEvent extends SplunkEvent {
	// ----------------------------------
		// DNS protocol
		// ----------------------------------

		/**
		 * The DNS domain that has been queried.
		 */
		public static String DNS_DEST_DOMAIN = "dest_domain";
		/**
		 * The remote DNS resource record being acted upon.
		 */
		public static String DNS_DEST_RECORD = "dest_record";
		/**
		 * The DNS zone that is being received by the slave as part of a zone
		 * transfer.
		 */
		public static String DNS_DEST_ZONE = "dest_zone";
		/**
		 * The DNS resource record class.
		 */
		public static String DNS_RECORD_CLASS = "record_class";
		/**
		 * The DNS resource record type.
		 *
		 * @see <a
		 *      href="https://secure.wikimedia.org/wikipedia/en/wiki/List_of_DNS_record_types">see
		 *      this Wikipedia article on DNS record types</a>
		 */
		public static String DNS_RECORD_TYPE = "record_type";
		/**
		 * The local DNS domain that is being queried.
		 */
		public static String DNS_SRC_DOMAIN = "src_domain";
		/**
		 * The local DNS resource record being acted upon.
		 */
		public static String DNS_SRC_RECORD = "src_record";
		/**
		 * The DNS zone that is being transferred by the master as part of a zone
		 * transfer.
		 */
		public static String DNS_SRC_ZONE = "src_zone";
		public void setDnsDestDomain(String dnsDestDomain) {
			addPair(DNS_DEST_DOMAIN, dnsDestDomain);
		}

		public void setDnsDestRecord(String dnsDestRecord) {
			addPair(DNS_DEST_RECORD, dnsDestRecord);
		}

		public void setDnsDestZone(String dnsDestZone) {
			addPair(DNS_DEST_ZONE, dnsDestZone);
		}

		public void setDnsRecordClass(String dnsRecordClass) {
			addPair(DNS_RECORD_CLASS, dnsRecordClass);
		}

		public void setDnsRecordType(String dnsRecordType) {
			addPair(DNS_RECORD_TYPE, dnsRecordType);
		}

		public void setDnsSrcDomain(String dnsSrcDomain) {
			addPair(DNS_SRC_DOMAIN, dnsSrcDomain);
		}

		public void setDnsSrcRecord(String dnsSrcRecord) {
			addPair(DNS_SRC_RECORD, dnsSrcRecord);
		}

		public void setDnsSrcZone(String dnsSrcZone) {
			addPair(DNS_SRC_ZONE, dnsSrcZone);
		}
		
}
