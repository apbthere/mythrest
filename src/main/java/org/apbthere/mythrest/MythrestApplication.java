package org.apbthere.mythrest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MythrestApplication {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(MythrestApplication.class, args);
	}

	@RequestMapping(value = "/control/{frontendIp}/{port}", method = RequestMethod.GET)
	public Object control(@PathVariable String frontendIp, @PathVariable int port, String command) {
		String reply = null;
		InetSocketAddress sockAdr = new InetSocketAddress(frontendIp, port);

		try (Socket sSocket = new Socket()) {
			sSocket.connect(sockAdr, 1000);

			try (OutputStream outputStream = sSocket.getOutputStream()) {
				PrintWriter sOutputStream = new PrintWriter(outputStream);

				logger.info("Sending command [{}]", command);
				sOutputStream.println("jump livetv");
				sOutputStream.flush();
				sOutputStream.println(command);
				sOutputStream.flush();
				
				try (InputStream inputStream = sSocket.getInputStream()) {
					BufferedReader sInputStream = new BufferedReader(new InputStreamReader(inputStream));
					reply = sInputStream.readLine();

					sInputStream.close();
					inputStream.close();
				}
				
				sOutputStream.close();
				outputStream.close();
			}

			sSocket.close();
		} catch (IOException e1) {
			logger.error("Catching", e1);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e1.getMessage());
		}
		return reply;
	}
}
