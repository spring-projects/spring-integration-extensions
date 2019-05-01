Spring Integration SMB Support
==============================

## Introduction

This module adds Spring Integration support for [Server Message Block][] (SMB).

[Server Message Block]: https://en.wikipedia.org/wiki/Server_Message_Block

## Version

[Versions in Maven Repository](http://central.maven.org/maven2/org/springframework/integration/spring-integration-smb/)

## Using Maven

Put the following block into pom.xml if using Maven:

    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-smb</artifactId>
        <version>1.0.0.RELEASE</version>
    </dependency>

## Changes

##### Version 1.1
 * Updated to use the latest version of the [JCIFS](https://github.com/codelibs/jcifs) library
 * SMB2 (2.02 protocol level) support, some SMB3 support

##### Version 1.2
 * Ability to set the SMB min/max versions in the `SmbSessionFactory` via configuration in the JCIFS library
 * Ability to use a custom implementation of the `jcifs.CIFSContext` interface in the `SmbSessionFactory`

## Overview

The Java CIFS Client Library has been chosen as a Java implementation for the CIFS/SMB networking protocol.
Its `SmbFile` abstraction is simply wrapped to the Spring Integration "Remote File" foundations like `SmbSession`, `SmbRemoteFileTemplate`, etc.

The SMB Channel Adapters and support classes implementations are fully similar to existing components for (S)FTP or AWS S3 protocols.
So, if you familiar with those components, it is pretty straightforward to use this extension. But any way here are several words about existing components:

### SMB Inbound Channel Adapter

To download SMB files locally the `SmbInboundFileSynchronizingMessageSource` is provided.
It is simple extension of the `AbstractInboundFileSynchronizingMessageSource` which requires `SmbInboundFileSynchronizer` injection.
For filtering remote files you still can use any existing `FileListFilter` implementations, but particular `SmbRegexPatternFileListFilter` and `SmbSimplePatternFileListFilter` are provided.
For XML configuration the `<int-smb:inbound-channel-adapter>` component is provided.

### SMB Outbound Channel Adapter

There is no (yet) some SMB specific requirements for files transferring to SMB, so for XML `<int-smb:outbound-channel-adapter>` component we simply reuse an existing `FileTransferringMessageHandler`.
In case of Java configuration that `FileTransferringMessageHandler` should be supplied with the `SmbSessionFactory` (or `SmbRemoteFileTemplate`).

````java
@ServiceActivator(inputChannel = "storeToSmb")
@Bean
public MessageHandler smbMessageHandler(SmbSessionFactory smbSessionFactory) {
    FileTransferringMessageHandler<SmbFile> handler =
                new FileTransferringMessageHandler<>(smbSessionFactory);
    handler.setRemoteDirectoryExpression(
                new LiteralExpression("remote-target-dir"));
    handler.setFileNameGenerator(m ->
                m.getHeaders().get(FileHeaders.FILENAME, String.class) + ".test");
    handler.setAutoCreateDirectory(true);
    return handler;
}
````

### Setting SMB Protocol Min/Max Versions

Example: To set a minimum version of SMB 2.1 and a maximum version of SMB 3.1.1

````java
@Bean
public SmbSessionFactory smbSessionFactory() {
    SmbSessionFactory smbSession = new SmbSessionFactory();
    smbSession.setHost("myHost");
    smbSession.setPort(445);
    smbSession.setDomain("myDomain");
    smbSession.setUsername("myUser");
    smbSession.setPassword("myPassword");
    smbSession.setShareAndDir("myShareAndDir");
    smbSession.setSmbMinVersion(DialectVersion.SMB210);
    smbSession.setSmbMaxVersion(DialectVersion.SMB311);
    return smbSession;
}
````

### Intializing SmbSessionFactory with a custom implementation of the jcifs.CIFSContext interface

NOTE: Setting of the SMB protocol min/max versions must be done in your implementation of jcifs.CIFSContext

````java
@Bean
public SmbSessionFactory smbSessionFactory(new myCIFSContext()) {
    SmbSessionFactory smbSession = new SmbSessionFactory();
    smbSession.setHost("myHost");
    smbSession.setPort(445);
    smbSession.setDomain("myDomain");
    smbSession.setUsername("myUser");
    smbSession.setPassword("myPassword");
    smbSession.setShareAndDir("myShareAndDir");
    return smbSession;
}
````
