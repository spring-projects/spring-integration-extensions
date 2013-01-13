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
