package org.device42.jenkins.device42secrets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.annotation.CheckForNull;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.device42.jenkins.api.Device42API;
import org.device42.jenkins.configuration.Device42Configuration;
import org.device42.jenkins.configuration.FolderDevice42Configuration;
import org.device42.jenkins.configuration.GlobalDevice42Configuration;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Run;
import hudson.util.Secret;
import okhttp3.OkHttpClient;

public class Device42SecretCredentialsImpl extends BaseStandardCredentials implements Device42SecretCredentials, StandardUsernamePasswordCredentials {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String device; // to be used as device
	private String user; // to be used as user

	private transient Device42Configuration device42Configuration;
	private transient Run<?, ?> context;

	private static final Logger LOGGER = Logger.getLogger(Device42SecretCredentialsImpl.class.getName());

	@DataBoundConstructor
	public Device42SecretCredentialsImpl(@CheckForNull CredentialsScope scope, @CheckForNull String id,
			@CheckForNull String device, @CheckForNull String user, @CheckForNull String description) {
		super(scope, id, description);
		this.device = device;
		this.user = user;
	}

	public String getDevice() {
		return this.device;
	}

	@DataBoundSetter
	public void setDevice(String device) {
		this.device = device;
	}

	public Secret getSecret() {
		String result = "";
		try {
			// Get Http Client
			OkHttpClient client = Device42API.getHttpClient(this.device42Configuration);
			// Authenticate to Device42
			// String credential = Credentials.basic("admin", "adm!nd42");
			String credential = Device42API.getAuthorizationToken(client, this.device42Configuration, context);
			// Retrieve secret from Device42
			String secretString = Device42API.getSecret(client, this.device42Configuration, credential, this.device,
					this.user);
			result = secretString;
		} catch (IOException e) {
			Writer writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			String s = writer.toString();
			LOGGER.log(Level.WARNING, "EXCEPTION: " + s);
			result = "EXCEPTION: " + e.getMessage();
		}
		return Secret.fromString(result);
	}

	@Override
	public String getDisplayName() {
		return "Device42Secret:" + this.device + ":" + this.user;
	}

	public void setContext(Run<?, ?> context) {
		LOGGER.log(Level.INFO, "Setting context");
		this.context = context;
		setDevice42Configuration(getConfigurationFromContext(context));
	}

	public void setDevice42Configuration(Device42Configuration device42Configuration) {
		this.device42Configuration = device42Configuration;
	}

	@SuppressWarnings("unchecked")
	protected Device42Configuration getConfigurationFromContext(Run<?, ?> context) {
		LOGGER.log(Level.INFO, "Getting Configuration from Context");
		Item job = context.getParent();
		Device42Configuration device42Config = GlobalDevice42Configuration.get().getDevice42Configuration();
		for (ItemGroup<? extends Item> g = job
				.getParent(); g instanceof AbstractFolder; g = ((AbstractFolder<? extends Item>) g).getParent()) {
			FolderDevice42Configuration fconf = ((AbstractFolder<?>) g).getProperties()
					.get(FolderDevice42Configuration.class);
			if (!(fconf == null || fconf.getInheritFromParent())) {
				// take the folder Device42 Configuration
				device42Config = fconf.getDevice42Configuration();
				break;
			}
		}
		LOGGER.log(Level.INFO, "<= " + device42Config.getApplianceURL());
		return device42Config;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Extension
	public static class DescriptorImpl extends CredentialsDescriptor {

		@Override
		public String getDisplayName() {
			return "Device42 Secret Credential";
		}
	}

	@Override
	public String getUsername() {
		return this.user;
	}

	@Override
	public Secret getPassword() {
		return this.getSecret();
	}

}
