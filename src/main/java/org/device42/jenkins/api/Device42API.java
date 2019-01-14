package org.device42.jenkins.api;

import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.device42.jenkins.configuration.Device42Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.CertificateCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import hudson.model.Run;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Device42API {

	private Device42API() {
		super();
	}

	private static final Logger LOGGER = Logger.getLogger(Device42API.class.getName());

	private static class Device42AuthnInfo {
		String applianceUrl;
		String account;
		String login;
		String apiKey;
	}

	private static Device42AuthnInfo getDevice42AuthnInfo(Device42Configuration configuration,
			List<UsernamePasswordCredentials> availableCredentials) {
		// Device42 variables
		Device42AuthnInfo device42Authn = new Device42AuthnInfo();

		if (configuration != null) {

			if (availableCredentials != null) {
				initializeWithCredential(device42Authn, configuration.getCredentialID(), availableCredentials);
			}

			String applianceUrl = configuration.getApplianceURL();
			if (applianceUrl != null && !applianceUrl.isEmpty()) {
				device42Authn.applianceUrl = applianceUrl;
			}
			String account = configuration.getAccount();
			if (account != null && !account.isEmpty()) {
				device42Authn.account = account;
			}
		}

		// Default to Environment variables if not values present
		defaultToEnvironment(device42Authn);

		return device42Authn;
	}

	private static void initializeWithCredential(Device42AuthnInfo device42Authn, String credentialID,
			List<UsernamePasswordCredentials> availableCredentials) {
		if (credentialID != null && !credentialID.isEmpty()) {
			LOGGER.log(Level.INFO, "Retrieving Device42 credential stored in Jenkins");
			UsernamePasswordCredentials credential = CredentialsMatchers.firstOrNull(availableCredentials,
					CredentialsMatchers.withId(credentialID));
			if (credential != null) {
				device42Authn.login = credential.getUsername();
				device42Authn.apiKey = credential.getPassword().getPlainText();
			}
		}
	}

	private static void defaultToEnvironment(Device42AuthnInfo device42Authn) {
		Map<String, String> env = System.getenv();
		if (device42Authn.applianceUrl == null && env.containsKey("DEVICE42_APPLIANCE_URL"))
			device42Authn.applianceUrl = env.get("DEVICE42_APPLIANCE_URL");
		if (device42Authn.account == null && env.containsKey("DEVICE42_ACCOUNT"))
			device42Authn.account = env.get("DEVICE42_ACCOUNT");
		if (device42Authn.login == null && env.containsKey("DEVICE42_AUTHN_LOGIN"))
			device42Authn.login = env.get("DEVICE42_AUTHN_LOGIN");
		if (device42Authn.apiKey == null && env.containsKey("DEVICE42_AUTHN_API_KEY"))
			device42Authn.apiKey = env.get("DEVICE42_AUTHN_API_KEY");
	}

	public static String getAuthorizationToken(OkHttpClient client, Device42Configuration configuration,
			Run<?, ?> context) throws IOException {

		String resultingToken = null;

		List<UsernamePasswordCredentials> availableCredentials = CredentialsProvider.lookupCredentials(
				UsernamePasswordCredentials.class, Jenkins.getInstance(), ACL.SYSTEM,
				Collections.<DomainRequirement>emptyList());

		if (context != null) {
			availableCredentials.addAll(CredentialsProvider.lookupCredentials(UsernamePasswordCredentials.class,
					context.getParent(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList()));
		}

		Device42AuthnInfo device42Authn = getDevice42AuthnInfo(configuration, availableCredentials);

		if (device42Authn.login != null && device42Authn.apiKey != null) {
			resultingToken = Credentials.basic(device42Authn.login, device42Authn.apiKey);
//			LOGGER.log(Level.INFO, "Authenticating with Device42");
//			Request request = new Request.Builder()
//					.url(String.format("%s/authn/%s/%s/authenticate", device42Authn.applianceUrl, device42Authn.account,
//							URLEncoder.encode(device42Authn.login, "utf-8")))
//					.post(RequestBody.create(MediaType.parse("text/plain"), device42Authn.apiKey)).build();
//
//			Response response = client.newCall(request).execute();
//			resultingToken = Base64.getEncoder().withoutPadding()
//					.encodeToString(response.body().string().getBytes("UTF-8"));
//			LOGGER.log(Level.INFO,
//					() -> "Device42 Authenticate response " + response.code() + " - " + response.message());
//			if (response.code() != 200) {
//				throw new IOException("Error authenticating to Device42 [" + response.code() + " - "
//						+ response.message() + "\n" + resultingToken);
//			}
		} else {
			LOGGER.log(Level.INFO, "Failed to find credentials for device42 authentication");
		}

		return resultingToken;
	}

	public static String getSecret(OkHttpClient client, Device42Configuration configuration, String credential,
			String device, String user) throws IOException {
		String result = null;

		Device42AuthnInfo device42Authn = getDevice42AuthnInfo(configuration, null);

		LOGGER.log(Level.INFO, "Fetching secret from Device42");
		Request request = new Request.Builder()
				.url(String.format("%s/api/1.0/passwords/?plain_text=yes&device=%s&username=%s",
						device42Authn.applianceUrl, device, user))
				.get().addHeader("Authorization", credential).addHeader("Content-Type", "application/json").build();

		LOGGER.log(Level.INFO, String.format("%s/api/1.0/passwords/?plain_text=yes&device=%s&username=%s",
				device42Authn.applianceUrl, device, user));
		LOGGER.log(Level.INFO, credential);
		Response response = client.newCall(request).execute();

		result = response.body().string();
		LOGGER.log(Level.INFO, () -> "Fetch secret [" + device + "] from Device42 response " + response.code() + " - "
				+ response.message());
		if (response.code() != 200) {
			throw new IOException("Error fetching secret from Device42 [" + response.code() + " - " + response.message()
					+ "\n" + result);
		}

		JSONObject jsonObj = new JSONObject(result);
		JSONArray Passwords = jsonObj.getJSONArray("Passwords");

//		System.out.println(jsonObj.toString());
//		System.out.println(Passwords.length());
//		
		jsonObj = (JSONObject) Passwords.get(0);
		Object password = jsonObj.get("password");
//		System.out.println(password.toString());
		LOGGER.log(Level.INFO,
				() -> "Fetch password [" + device + "] from Device42 response " + user + " - " + password.toString());
		return password.toString();
	}

	public static OkHttpClient getHttpClient(Device42Configuration configuration) {

		OkHttpClient client = null;

		CertificateCredentials certificate = CredentialsMatchers.firstOrNull(
				CredentialsProvider.lookupCredentials(CertificateCredentials.class, Jenkins.getInstance(), ACL.SYSTEM,
						Collections.<DomainRequirement>emptyList()),
				CredentialsMatchers.withId(configuration.getCertificateCredentialID()));

		if (certificate != null) {
			try {

				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(certificate.getKeyStore(), certificate.getPassword().getPlainText().toCharArray());
				KeyManager[] kms = kmf.getKeyManagers();

				KeyStore trustStore = KeyStore.getInstance("JKS");
				trustStore.load(null, null);
				Enumeration<String> e = certificate.getKeyStore().aliases();
				while (e.hasMoreElements()) {
					String alias = e.nextElement();
					trustStore.setCertificateEntry(alias, certificate.getKeyStore().getCertificate(alias));
				}
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(trustStore);
				TrustManager[] tms = tmf.getTrustManagers();

				SSLContext sslContext = null;
				sslContext = SSLContext.getInstance("TLSv1.2");
				sslContext.init(kms, tms, new SecureRandom());

				client = new OkHttpClient.Builder()
						.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) tms[0]).build();
			} catch (Exception e) {
				throw new IllegalArgumentException("Error configuring server certificates.", e);
			}
		} else {
		    final TrustManager[] trustAllCerts = new TrustManager[1];
		    trustAllCerts[0] = new MyX509TrustManager();

		    // Install the all-trusting trust manager
		    SSLContext sslContext;
			try {
				sslContext = SSLContext.getInstance("SSL");
			    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			    // Create an ssl socket factory with our all-trusting manager
			    final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			    OkHttpClient.Builder builder = new OkHttpClient.Builder();
			    builder.sslSocketFactory(sslSocketFactory, (javax.net.ssl.X509TrustManager)trustAllCerts[0]);
			    builder.hostnameVerifier(new MyHostnameVerifier());
			    		
			    client = builder.build();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				client = new OkHttpClient.Builder().build();
			}
		}

		return client;
	}

}
