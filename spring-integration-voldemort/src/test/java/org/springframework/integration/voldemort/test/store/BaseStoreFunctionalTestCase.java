/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.voldemort.test.store;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.voldemort.store.VoldemortMessageStore;
import org.springframework.integration.voldemort.test.BaseFunctionalTestCase;

/**
 * Base class for message store test cases.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public abstract class BaseStoreFunctionalTestCase extends BaseFunctionalTestCase {
	protected ClassPathXmlApplicationContext context = null;
	protected VoldemortMessageStore store = null;

	@Override
	protected File getStoreConfiguration() {
		return new File( "src/test/resources/org/springframework/integration/voldemort/test/store/stores.xml" );
	}

	@Before
	public void contextSetup() {
		context = new ClassPathXmlApplicationContext( "store-test-context.xml", getClass() );
		store = context.getBean( "voldemortMessageStore", VoldemortMessageStore.class );
	}

	@After
	public void contextDestroy() {
		store = null;
		if ( context != null ) {
			context.close();
			context = null;
		}
	}

	protected VoldemortMessageStore createNewStoreClient() {
		return context.getBean( "voldemortMessageStorePrototype", VoldemortMessageStore.class );
	}
}
