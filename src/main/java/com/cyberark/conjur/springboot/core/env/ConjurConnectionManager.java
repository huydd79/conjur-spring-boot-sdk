package com.cyberark.conjur.springboot.core.env;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.Configuration;

/**
 * 
 * This is the connection creation singleton class with conjur vault by using
 * the conjur java sdk.
 *
 */
public final class ConjurConnectionManager {

	private static ConjurConnectionManager conjurConnectionInstance = null;

	private static Logger logger = LoggerFactory.getLogger(ConjurConnectionManager.class);

	// For Getting Connection with conjur vault using cyberark sdk
	private ConjurConnectionManager() {

		getConnection();

	}

	private void getConnection() { 
		String token="";		
		try {
			token = Files.readString(Paths.get(System.getenv("CONJUR_ACCESS_TOKEN_FILE")));
		} catch (Exception e){
			logger.error(e.getMessage());
		}

		try {
			ApiClient client = Configuration.getDefaultApiClient();
			if (token.isBlank()){
				AccessToken accesToken = client.getNewAccessToken();
				if (accesToken == null) {
					logger.error("Access token is null, Please enter proper environment variables.");
				}
				token = accesToken.getHeaderValue();
				logger.info("Token does not exist, getting new token.");					
				logger.debug("New token: " + token);
			} else {
				logger.info("Token existed, using current value.");
				logger.debug("Current token: " + token);
			}

			client.setAccessToken(token);
			Configuration.setDefaultApiClient(client);
			logger.debug("Connection with conjur is successful");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	/**
	 * method to create instance of class and checking for multiple threads.
	 * @return unique instance of class. 
	 */
	public static ConjurConnectionManager getInstance() {
		if (conjurConnectionInstance == null) {
			synchronized (ConjurConnectionManager.class) {
				if (conjurConnectionInstance == null) {
					conjurConnectionInstance = new ConjurConnectionManager();
				}
			}
		}
		return conjurConnectionInstance;
	}
}
