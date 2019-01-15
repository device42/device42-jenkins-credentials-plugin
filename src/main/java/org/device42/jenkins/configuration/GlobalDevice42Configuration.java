package org.device42.jenkins.configuration;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundSetter;

/**
 * Example of Jenkins global configuration.
 */
@Extension
public class GlobalDevice42Configuration extends GlobalConfiguration {

	private Device42Configuration device42Configuration;

	/** @return the singleton instance */
	@Nonnull
	public static GlobalDevice42Configuration get() {
		GlobalDevice42Configuration result = GlobalConfiguration.all().get(GlobalDevice42Configuration.class);
		if (result == null) {
			throw new IllegalStateException();
		}
		return result;
	}

	public GlobalDevice42Configuration() {
		// When Jenkins is restarted, load any saved configuration from disk.
		load();
	}

	public Device42Configuration getDevice42Configuration() {
		return device42Configuration;
	}

	@DataBoundSetter
	public void setDevice42Configuration(Device42Configuration device42Configuration) {
		this.device42Configuration = device42Configuration;
		save();
	}

}
