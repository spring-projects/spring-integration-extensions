Spring Integration XMPP Extension for Smack 4.1
===============================================

This extension is provided because we can't upgrade the module within the main distribution to [Smack 4.1](https://www.igniterealtime.org/projects/smack/).
It has API changes that cannot be accommodated in a point release of Spring Integration 4.2.
The upgrade is postponned to the [Spring Integration 4.3](https://jira.spring.io/browse/INT-3834), at which time,
this version will be merged back to the main project.

So, if you intend to use Smack 4.1 with existing Spring Integration 4.2, this extension is for you.

The functionally of this project is fully equivalent to the Spring Integration XMPP module, so you can rely on the
[Spring Integration Reference Manual](http://docs.spring.io/spring-integration/reference/html/xmpp.html)
for more information.
