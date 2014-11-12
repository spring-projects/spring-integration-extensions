package org.springframework.integration.smpp.core;

import org.jsmpp.bean.DataCoding;
import org.jsmpp.bean.DataCodings;
import org.jsmpp.bean.SimpleDataCoding;
import org.junit.Test;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Flemming Jønsson
 */
public class SmesMessageSpecificationTest {

    private static final int MULTISEGMENT_PREFIX_BYTES = 5;

    private static final String MULTILINE_PAYLOAD =
            "1: This is a string longer than 140 chars and it contains newlines and foreign characters - æøå. This is the first line. \n" +
                    "2: This is the second line. \n" +
                    "3: This is the third line\n" +
                    "4: Fourth line\n" +
                    "5: Fifth line\n" +
                    "6: Sixth line\n" +
                    "7: Seventh line\n" +
                    "8: Eighth line\n" +
                    "9: Ninth line\n" +
                    "The end";

    private static final String SINGLELINE_PAYLOAD = "1: This is a line that is longer than what can be sent in a single segment. It also has foreign characters æøå. Lorem ipsum dolor sit amet, atqui iudico epicuri mel eu. Cu quaestio voluptatum ullamcorper quo, nec ut numquam habemus, summo nostrum est ut. Nominati praesent repudiare at sit, ut tibique suscipit has, nec eu odio erat qualisque. Soleat ridens ut nec, eu denique mentitum his.";

    /* ********** Single line tests ************* */

    @Test
    public void singleLineSplitsCorrectlyFor7Bit() throws Exception {
        DataCoding dataCoding = DataCodings.newInstance((byte) 0);

        verifySingleLineMessageSplitCorrectlyForDataCoding(dataCoding);
    }

    @Test
    public void singleLineSplitsCorrectlyFor8Bit() throws Exception {
        DataCoding dataCoding = DataCodings.newInstance((byte) 4);

        verifySingleLineMessageSplitCorrectlyForDataCoding(dataCoding);
    }

    @Test
    public void singleLineSplitsCorrectlyFor16Bit() throws Exception {
        DataCoding dataCoding = DataCodings.newInstance((byte) 8);

        verifySingleLineMessageSplitCorrectlyForDataCoding(dataCoding);
    }

    @Test
    public void singleLineSplitsCorrectlyForSimpleDataCoding() throws Exception {
        DataCoding dataCoding = new SimpleDataCoding();

        verifySingleLineMessageSplitCorrectlyForDataCoding(dataCoding);
    }

    @Test
    public void singleLineSplitsCorrectlyForDefault() throws Exception {
        verifyMessageSplitCorrectlyForDataCoding(SINGLELINE_PAYLOAD, null, null);
    }

    @Test
    public void singleLineSplitsCorrectlyForCustomMaxCharacters() throws Exception {
        verifyMessageSplitCorrectlyForDataCoding(SINGLELINE_PAYLOAD, null, 99);
    }

    /* ********** Multi line tests ************* */

    @Test
    public void multiLineSplitsCorrectlyFor7Bit() throws Exception {
        DataCoding dataCoding = DataCodings.newInstance((byte) 0);

        verifyMultilineMessageSplitCorrectlyForDataCoding(dataCoding);
    }

    @Test
    public void multiLineSplitsCorrectlyFor8Bit() throws Exception {
        DataCoding dataCoding = DataCodings.newInstance((byte) 4);

        verifyMultilineMessageSplitCorrectlyForDataCoding(dataCoding);
    }

    @Test
    public void multiLineSplitsCorrectlyFor16Bit() throws Exception {
        DataCoding dataCoding = DataCodings.newInstance((byte) 8);

        verifyMultilineMessageSplitCorrectlyForDataCoding(dataCoding);
    }

    @Test
    public void multiLineSplitsCorrectlyForSimpleDataCoding() throws Exception {
        DataCoding dataCoding = new SimpleDataCoding();

        verifyMultilineMessageSplitCorrectlyForDataCoding(dataCoding);
    }

    @Test
    public void multiLineSplitsCorrectlyForDefault() throws Exception {
        verifyMessageSplitCorrectlyForDataCoding(MULTILINE_PAYLOAD, null, null);
    }

    @Test
    public void multiLineSplitsCorrectlyForCustomMaxCharacters() throws Exception {
        verifyMessageSplitCorrectlyForDataCoding(MULTILINE_PAYLOAD, null, 99);
    }

    /* ***************** Helper methods ****************** */

    private void verifySingleLineMessageSplitCorrectlyForDataCoding(DataCoding dataCoding) throws UnsupportedEncodingException {
        verifyMessageSplitCorrectlyForDataCoding(SINGLELINE_PAYLOAD, dataCoding, null);
    }

    private void verifyMultilineMessageSplitCorrectlyForDataCoding(DataCoding dataCoding) throws UnsupportedEncodingException {
        verifyMessageSplitCorrectlyForDataCoding(MULTILINE_PAYLOAD, dataCoding, null);
    }

    private void verifyMessageSplitCorrectlyForDataCoding(String payload, DataCoding dataCoding, Integer maxMessageSize) throws UnsupportedEncodingException {
        SmesMessageSpecification smesMessageSpecification = getSmesMessageSpecification(payload, dataCoding, maxMessageSize);

        if (dataCoding == null) {
            dataCoding = (DataCoding) ReflectionTestUtils.getField(smesMessageSpecification, "dataCoding");
        }
        int maxCharacters;
        if (maxMessageSize == null) {
            maxCharacters = DataCodingSpecification.getMaxCharacters(dataCoding.toByte());
        } else {
            maxCharacters = maxMessageSize;
        }

        int maxCharactersWithRoomForPrefix = maxCharacters - MULTISEGMENT_PREFIX_BYTES;
        String charsetName = DataCodingSpecification.getCharsetName(dataCoding.toByte());

        List<byte[]> messageParts = (List<byte[]>) ReflectionTestUtils.getField(smesMessageSpecification, "shortMessageParts");

        int expectedPartsCountAfterSplit = (payload.length() + maxCharactersWithRoomForPrefix - 1) / maxCharactersWithRoomForPrefix;

        //No logger impl in gradle test deps?
        if (Boolean.getBoolean("show_test_output")) {
            System.out.println("Datacoding[" + dataCoding.getClass().getSimpleName() + " " + dataCoding.toByte() + "] maxCharacters: " + maxCharacters + ", maxCharactersWithRoomForPrefix: " + maxCharactersWithRoomForPrefix + ", payload length: " + payload.length() + ", expectedPartsCountForDataCoding: " + expectedPartsCountAfterSplit);
        }

        assertEquals("Message parts count after split does not match expected size", expectedPartsCountAfterSplit, messageParts.size());

        //Assert and fail if individual parts are longer than allowed max size for this coding (or MAXIMUM_CHARACTERS if set)
        for (byte[] messagePart : messageParts) {
            String string = new String(messagePart, charsetName);
            assertTrue("MessagePart is longer than allowed max message size for data coding - Actual part length: " + string.length() + " vs. max size: " + maxCharacters, string.length() <= maxCharacters);
        }

    }

    private SmesMessageSpecification getSmesMessageSpecification(String payload, DataCoding dataCoding, Integer maxMessageSize) {
        MessageBuilder<String> messageBuilder = MessageBuilder.withPayload(payload);
        messageBuilder.setHeaderIfAbsent(SmppConstants.SMS_MSG, payload);
        if (dataCoding != null) {
            messageBuilder.setHeaderIfAbsent(SmppConstants.DATA_CODING, dataCoding);
        }
        if (maxMessageSize != null) {
            messageBuilder.setHeaderIfAbsent(SmppConstants.MAXIMUM_CHARACTERS, maxMessageSize);
        }
        Message<String> message = messageBuilder.build();

        return SmesMessageSpecification.fromMessage(null, message);
    }
}
