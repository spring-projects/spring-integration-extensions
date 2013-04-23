package org.springframework.integration.kafka.serializer.common;

import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;

import java.util.Properties;

/**
 * @author Soby Chacko
 */
public class StringEncoder implements Encoder {

    private String encoding = "UTF8";

    public void setEncoding(final String encoding){
        this.encoding = encoding;
    }

    @Override
    public byte[] toBytes(Object o) {
        final Properties props = new Properties();
        props.put("serializer.encoding", encoding);
        final VerifiableProperties verifProperties = new VerifiableProperties(props);
        return new kafka.serializer.StringEncoder(verifProperties).toBytes((String)o);
    }
}
