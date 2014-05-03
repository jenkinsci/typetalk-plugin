package org.jenkinsci.plugins.typetalk;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;

public class TypetalkNotifier extends Notifier {

	public final String name;
	public final String topicNumber;
	public final boolean notifyWhenSuccess;

	@DataBoundConstructor
	public TypetalkNotifier(String name, String topicNumber, boolean notifyWhenSuccess) {
		this.name = name;
		this.topicNumber = topicNumber;
		this.notifyWhenSuccess = notifyWhenSuccess;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		final String rootUrl = Jenkins.getInstance().getRootUrl();
		if (StringUtils.isEmpty(rootUrl)) {
			throw new IllegalStateException("Root URL isn't configured yet. Cannot compute absolute URL.");
		}

		// 前回からビルド成功で "ビルドが成功した場合も通知する" がオフの場合、通知しない
		if (successFromPreviousBuild(build) && notifyWhenSuccess == false) {
			return true;
		}

		// Typetalkに通知中...
		listener.getLogger().println("Notifying to the Typetalk...");

		Credential credential = getDescriptor().getCredential(name);
		if (credential == null) {
			throw new IllegalStateException("Credential is not found.");
		}
		Typetalk typetalk = new Typetalk(credential.getClientId(), credential.getClientSecret());

		String message = makeMessage(build, TypetalkResult.convert(build), rootUrl);
		Long topicId = Long.valueOf(topicNumber);
		typetalk.postMessage(topicId, message);

		return true;
	}

	private String makeMessage(AbstractBuild<?, ?> build, TypetalkResult typetalkResult, String rootUrl) {
		final StringBuilder message = new StringBuilder();
		message.append(typetalkResult);
		message.append(" [ ");
		message.append(build.getProject().getDisplayName());
		message.append(" ]");
		message.append("\n");
		message.append(rootUrl);
		message.append(build.getUrl());
		return message.toString();
	}

	private boolean successFromPreviousBuild(AbstractBuild<?, ?> build) {
		if (build.getPreviousBuild() == null) {
			return build.getResult().equals(Result.SUCCESS);
		} else {
			return build.getResult().equals(Result.SUCCESS)
				&& build.getPreviousBuild().getResult().equals(Result.SUCCESS);
		}
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {

		private volatile Credential[] credentials = new Credential[0];

		public Credential[] getCredentials() {
			return credentials;
		}

		public Credential getCredential(String name) {
			for (Credential credential : credentials) {
				if (credential.getName().equals(name)) {
					return credential;
				}
			}
			return null;
		}

		public DescriptorImpl() {
			super(TypetalkNotifier.class);
			load();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Typetalk Notification";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			try {
				credentials = req.bindJSONToList(Credential.class,
						req.getSubmittedForm().get("credential")).toArray(new Credential[0]);
				save();
				return true;
			} catch (ServletException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	public static final class Credential implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String name;

		private final String clientId;

		private final Secret clientSecret;

		@DataBoundConstructor
		public Credential(String name, String clientId, String clientSecret) {
			this.name = name;
			this.clientId = clientId;
			this.clientSecret = Secret.fromString(clientSecret);
		}

		public String getName() {
			return name;
		}

		public String getClientId() {
			return clientId;
		}

		public String getClientSecret() {
			return Secret.toString(clientSecret);
		}

	}

}
