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
package org.springframework.integration.splunk.support;

/**
 * Search mode supported by Splunk.
 *
 * Blocking: Run synchronous search API
 * Normal: Run asynchronous search API
 * Realtime: Run the searches which are over a defined real time window
 * Export: Run synchronously in your code , best way for bulk exports of events from Splunk
 * Saved: Run predefined searches/parameters that are saved in Splunk in a namespace and you can execute them by name
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public enum SearchMode {
	BLOCKING, NORMAL, REALTIME, EXPORT, SAVEDSEARCH;
}