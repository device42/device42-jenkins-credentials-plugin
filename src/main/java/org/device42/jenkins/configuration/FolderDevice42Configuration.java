package org.device42.jenkins.configuration;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;

import hudson.Extension;

public class FolderDevice42Configuration extends AbstractFolderProperty<AbstractFolder<?>> {
	
    private Boolean inheritFromParent = true;
	private Device42Configuration device42Configuration;

    @DataBoundConstructor
    public FolderDevice42Configuration(Device42Configuration device42Configuration) {
    	super();
        this.device42Configuration = device42Configuration;
    }

	public Device42Configuration getDevice42Configuration() {
		return device42Configuration;
	}

	@DataBoundSetter
	public void setDevice42Configuration(Device42Configuration device42Configuration) {
		this.device42Configuration = device42Configuration;
	}

	public Boolean getInheritFromParent() {
		return inheritFromParent;
	}

	@DataBoundSetter
	public void setInheritFromParent(Boolean inheritFromParent) {
		this.inheritFromParent = inheritFromParent;
	}

	@Extension
    public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {
    }
}
