/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.x.ip.sockjs.serializer;


/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class XHRStreamingChunkDeserializer {// extends AbstractSockJsDeserializer {

//	@Override
//	public List<DataFrame> deserialize(InputStream inputStream) throws IOException {
//		List<DataFrame> headers = checkStreaming(inputStream);
//		if (headers != null) {
//			return headers;
//		}
//		SockJsState state = (SockJsState) this.getStreamState(inputStream);
//		boolean gzipping = state == null ? false : state.isGzipping();
//		boolean complete = false;
//		StringBuilder builder = new StringBuilder();
//		while (!complete) {
//			byte[] chunkLengthInHex = this.crlfDeserializer.deserialize(inputStream);
//			if (chunkLengthInHex.length == 0) {
//				break;
//			}
//			int chunkLength = 0;
//			try {
//				chunkLength = Integer.parseInt(new String(chunkLengthInHex, "UTF-8"), 16);
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//			if (logger.isDebugEnabled()) {
//				logger.debug("Chunk size = " + chunkLength);
//			}
//			if (chunkLength <= 0) {
//				throw new SoftEndOfStreamException("0 length chunk received");
//			}
//			byte[] chunk = new byte[chunkLength];
//			for (int i = 0; i < chunkLength; i++) {
//				int c = inputStream.read();
//				checkClosure(c);
//				chunk[i] = (byte) c;
//			}
//			int eom = chunk[chunkLength-1];
//			Assert.isTrue(inputStream.read() == '\r', "Expected \\r");
//			Assert.isTrue(inputStream.read() == '\n', "Expected \\n");
//			int adjust = complete ? 1 : 0;
//			String data;
//			if (gzipping) {
//				byte[] unzipped = new byte[this.maxMessageSize];
//				GZIPFeederInputStream feeder = state.getGzipFeederInputStream();
//				ByteArrayInputStream byteStream = new ByteArrayInputStream(chunk, 0, chunkLength - adjust);
//				feeder.setInputStream(byteStream);
//				GZIPInputStream gzipInputStream = state.getGzipInputStream();
//				while (byteStream.available() > 0) {
//					int decodedLength = gzipInputStream.read(unzipped);
//					if (logger.isDebugEnabled()) {
//						logger.debug("Inflated to " + decodedLength);
//					}
//					data = new String(unzipped, 0, decodedLength, "UTF-8");
//					builder.append(data);
//				}
//				eom = builder.charAt(builder.length()-1);
//			}
//			else {
//				data = new String(chunk, 0, chunkLength - adjust, "UTF-8");
//				builder.append(data);
//			}
//			complete = eom == '\n';
////			System.out.println(data.length() + ":" + data);
//		}
//		return this.decodeFrameData(builder.toString());
//	}
//
//	List<DataFrame> decodeFrameData(String frameData) throws IOException {
//		List<DataFrame> dataList = new ArrayList<DataFrame>();
//		// some servers put multiple frames in the same chunk
//		String[] frames;
//		if (frameData.contains("\n")) {
//			frames = frameData.split("\n");
//		}
//		else {
//			frames = new String[] {frameData};
//		}
//		for (String data : frames) {
//			dataList.add(this.decodeToFrame(data));
//		}
//		return dataList;
//	}
//
}
