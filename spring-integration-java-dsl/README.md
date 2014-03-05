Spring Integration Java DSL
===============================

The Spring Integration JavaConfig and DSL extension. Provides a set of convenient Builders and fluent API to configure
Spring Integration message flows from Spring `@Configuration` classes.

## Example Configurations

````java
    @Configuration
    @EnableIntegration
    public class MyConfiguration {

        @Bean
        public MessageSource<?> integerMessageSource() {
            MethodInvokingMessageSource source = new MethodInvokingMessageSource();
            source.setObject(new AtomicInteger());
            source.setMethodName("getAndIncrement");
            return source;
        }

        @Bean
        public DirectChannel inputChannel() {
            return new DirectChannel();
        }

        @Bean
        public IntegrationFlow myFlow() {
            return IntegrationFlows.from(this.integerMessageSource(), c -> c.poller(Pollers.fixedRate(100)))
                        .channel(this.inputChannel())
                        .filter((Integer p) -> p > 0)
                        .transform(Object::toString)
                        .channel(MessageChannels.queue())
                        .get();
        }
    }
````

As the result after `ApplicationContext` start up will be created Spring Integration endpoints and Message Channels as it is after XML parsing.
Such configuration can be used to replace XML configuration or together with that.

## Maven

### Repository

    <repository>
        <id>repository.springframework.maven.snapshot</id>
        <name>Spring Framework Maven Snapshot Repository</name>
        <url>http://repo.spring.io/libs-snapshot</url>
    </repository>

### Artifact

    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-dsl</artifactId>
        <version>1.0.0.BUILD-SNAPSHOT</version>
    </dependency>

## Support

Check out the [Spring Integration forums][] and the [spring-integration][spring-integration tag] tag
on [Stack Overflow][]. [Commercial support][] is available, too.

## Related GitHub projects

* [Spring Integration][]
* [Spring Integration Samples][]
* [Spring Integration Templates][]
* [Spring Integration Dsl Groovy][]
* [Spring Integration Dsl Scala][]
* [Spring Integration Pattern Catalog][]

For more information, please also don't forget to visit the [Spring Integration][] website.

[Spring Integration]: https://github.com/spring-projects/spring-integration
[Commercial support]: http://springsource.com/support/springsupport
[Spring Integration forums]: http://forum.spring.io/forum/spring-projects/integration
[spring-integration tag]: http://stackoverflow.com/questions/tagged/spring-integration
[Spring Integration Samples]: https://github.com/spring-projects/spring-integration-samples
[Spring Integration Templates]: https://github.com/spring-projects/spring-integration-templates/tree/master/si-sts-templates
[Spring Integration Dsl Groovy]: https://github.com/spring-projects/spring-integration-dsl-groovy
[Spring Integration Dsl Scala]: https://github.com/spring-projects/spring-integration-dsl-scala
[Spring Integration Pattern Catalog]: https://github.com/spring-projects/spring-integration-pattern-catalog
[Stack Overflow]: http://stackoverflow.com/faq



