/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.cassandra.outbound.driver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;
import org.springframework.integration.cassandra.outbound.CassandraStoringMessageHandler;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import com.datastax.driver.core.Session;

import java.util.UUID;

/**
 * Simple Spring Boot app to run the outbound adapter.
 *
 * This class requires a Cassandra instance running and
 * configured in cassandra.properties in src/test/resources.
 *
 * Keyspace used is called "demo"
 *
 * @author Soby Chacko
 */
@SpringBootApplication
public class CassandraStoringMessageHandlerDriver implements CommandLineRunner {

    @Autowired
    @Qualifier("cassandraStoringMessageHandler")
    private MessageHandler messageHandler;

    @Override
    public void run(String... args) throws Exception {
        Users users = new Users();
        users.setLastname(String.format("Bar-%s", UUID.randomUUID()));
        users.setFirstname("Foo");
        users.setEmail("foo@bar.com");
        users.setCity("New York");
        users.setAge(40);

        Message<Users> message = MessageBuilder.withPayload(users).build();
        messageHandler.handleMessage(message);
    }

    public static void main(String... args) {
        SpringApplication.run(CassandraStoringMessageHandlerDriver.class, args);
    }

    @Table
    public class Users {

        @PrimaryKey(value = "lastname")
        String lastname;
        String firstname;
        String email;
        String city;
        int age;

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Configuration
    @EnableIntegration
    @PropertySource(value = { "classpath:cassandra.properties" })
    static class CassandraConfiguration {

        @Autowired
        private Environment env;

        @Autowired
        private ApplicationContext context;

        @Bean
        public MessageHandler cassandraStoringMessageHandler() {
            return new CassandraStoringMessageHandler<Users>(cassandraTemplate());
        }

        @Bean
        public CassandraClusterFactoryBean cluster() {

            CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
            cluster.setContactPoints(env.getProperty("cassandra.contactpoints"));
            cluster.setPort(Integer.parseInt(env.getProperty("cassandra.port")));

            return cluster;
        }

        @Bean
        public CassandraMappingContext mappingContext() {
            return new BasicCassandraMappingContext();
        }

        @Bean
        public CassandraConverter converter() {
            return new MappingCassandraConverter(mappingContext());
        }

        @Bean
        public CassandraSessionFactoryBean session() throws Exception {

            CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
            session.setCluster(cluster().getObject());
            session.setKeyspaceName(env.getProperty("cassandra.keyspace"));
            session.setConverter(converter());
            session.setSchemaAction(SchemaAction.NONE);

            return session;
        }

        @Bean
        public CassandraOperations cassandraTemplate()  {
            Session session = context.getBean(Session.class);
            return new CassandraTemplate(session);
        }
    }

}
