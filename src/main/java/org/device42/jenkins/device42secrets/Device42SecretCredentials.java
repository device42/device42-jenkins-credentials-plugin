package org.device42.jenkins.device42secrets;

import org.device42.jenkins.configuration.Device42Configuration;
import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import hudson.Extension;
import hudson.model.Run;
import hudson.util.Secret;

@NameWith(value=Device42SecretCredentials.NameProvider.class, priority = 1)

public interface Device42SecretCredentials extends StandardCredentials {
	
	String getDisplayName();
	Secret getSecret();
	void setDevice42Configuration(Device42Configuration device42Configuration);
	void setContext(Run<?, ?> context);
		
	class NameProvider extends CredentialsNameProvider<Device42SecretCredentials> {

		@Override
		public String getName(Device42SecretCredentials device42SecretCredential) {
			String description = device42SecretCredential.getDescription();
			return device42SecretCredential.getDisplayName()
					+ "/*Device42*"
					+ " (" + description + ")";
		}
		
	}
	
//    @Extension
//    public static class DescriptorImpl extends BindingDescriptor<Device42SecretCredentials> {
//		
//        @Override
//        protected Class<Device42SecretCredentials> type() {
//            return Device42SecretCredentials.class;
//        }
//
//        @Override
//        public String getDisplayName() {
//            return "Device42 Secret Credential";
//        }
//
//    }	

}
