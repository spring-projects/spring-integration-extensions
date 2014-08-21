/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.integration.dsl.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;

import org.apache.sshd.common.util.Base64;

/**
 * @author Gary Russell
 *
 */
public class PoorMansMailServer {

	public SmtpServer smtp(int port) {
		try {
			return new SmtpServer(port);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public class SmtpServer implements Runnable {

		private final ServerSocket socket;

		private final ExecutorService exec = Executors.newCachedThreadPool();

		private final List<String> messages = new ArrayList<String>();

		private volatile boolean listening;

		public SmtpServer(int port) throws IOException {
			this.socket = ServerSocketFactory.getDefault().createServerSocket(port);
			this.listening = true;
			exec.execute(this);
		}

		public boolean isListening() {
			return listening;
		}

		public List<String> getMessages() {
			return messages;
		}

		@Override
		public void run() {
			try {
				while (!socket.isClosed()) {
					Socket socket = this.socket.accept();
					exec.execute(new SmtpHandler(socket));
				}
			}
			catch (IOException e) {
				this.listening = false;
			}
		}

		public void stop() {
			try {
				this.socket.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			this.exec.shutdownNow();
		}

		public class SmtpHandler implements Runnable {

			private final Socket socket;

			private BufferedWriter writer;

			public SmtpHandler(Socket socket) {
				this.socket = socket;
			}

			@Override
			public void run() {
				try {
					StringBuilder sb = new StringBuilder();
					BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
					this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
					write("220 foo SMTP");
					while (!socket.isClosed()) {
						String line = reader.readLine();
//						System.out.println(line);
						if (line.contains("EHLO")) {
							write("250-foo hello [0,0,0,0], foo");
							write("250-AUTH LOGIN PLAIN");
							write("250 OK");
						}
						else if (line.contains("MAIL FROM")) {
							write("250 OK");
						}
						else if (line.contains("RCPT TO")) {
							write("250 OK");
						}
						else if (line.contains("AUTH LOGIN")) {
							write("334 VXNlcm5hbWU6");
						}
						else if (line.contains("dXNlcg==")) { // base64 'user'
							sb.append("user:");
							sb.append((new String(new Base64().decode(line.getBytes()))));
							sb.append("\n");
							write("334 UGFzc3dvcmQ6");
						}
						else if (line.contains("cHc=")) { // base64 'pw'
							sb.append("password:");
							sb.append((new String(new Base64().decode(line.getBytes()))));
							sb.append("\n");
							write("235");
						}
						else if (line.equals("DATA")) {
							write("354");
						}
						else if (line.equals(".")) {
							write("250");
						}
						else if (line.equals("QUIT")) {
							write("221");
							socket.close();
						}
						else {
							sb.append(line);
							sb.append("\n");
						}
					}
					messages.add(sb.toString());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}

			private void write(String str) throws IOException {
				this.writer.write(str);
				this.writer.write("\r\n");
				this.writer.flush();
			}


		}

	}

}
