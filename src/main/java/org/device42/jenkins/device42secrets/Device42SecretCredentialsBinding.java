package org.device42.jenkins.device42secrets;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;
import org.jenkinsci.plugins.credentialsbinding.MultiBinding;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

public class Device42SecretCredentialsBinding extends MultiBinding<Device42SecretCredentials> {
	
	private String variable;

	private static final Logger LOGGER = Logger.getLogger( Device42SecretCredentialsBinding.class.getName());
	
	@DataBoundConstructor
	public Device42SecretCredentialsBinding(String credentialsId) {
		super(credentialsId);
	}
	
	public String getVariable() {
		return this.variable;
	}
	
    @DataBoundSetter
	public void setVariable(String variable) {
    	LOGGER.log(Level.INFO, "Setting variable to {0}", variable);
		this.variable = variable;
	}
	
    @Override
	public MultiEnvironment bind(Run<?, ?> build, FilePath workSpace, Launcher launcher, TaskListener listener)
			throws IOException, InterruptedException {

		Device42SecretCredentials device42SecretCredential = getCredentials(build);
		device42SecretCredential.setContext(build);

		return new MultiEnvironment(Collections.singletonMap(variable, device42SecretCredential.getSecret().getPlainText()));
	}        

	@Override
	protected Class<Device42SecretCredentials> type() {
		return Device42SecretCredentials.class;
	}

	
	@Symbol("device42SecretCredential")
	@Extension
    public static class DescriptorImpl extends BindingDescriptor<Device42SecretCredentials> {

		@Override 
		public boolean requiresWorkspace() {
            return false;
        }
		
        @Override
        protected Class<Device42SecretCredentials> type() {
            return Device42SecretCredentials.class;
        }

        @Override
        public String getDisplayName() {
            return "Device42 Secret credentials";
        }
    }


	@Override
	public Set<String> variables() {
		return Collections.singleton(variable);
	}
		
}
