package org.jenkinsci.plugins.typetalk;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.jenkinsci.plugins.typetalk.delegate.BuildWrapperDelegate;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class TypetalkBuildWrapper extends BuildWrapper {

	public final String name;
	public final String topicNumber;
	public final String talkNumber;
	public final boolean notifyStart;
	public final String notifyStartMessage;
	public final boolean notifyEnd;
	public final String notifyEndMessage;

	@DataBoundConstructor
	public TypetalkBuildWrapper(String name, String topicNumber, String talkNumber, boolean notifyStart, String notifyStartMessage, boolean notifyEnd, String notifyEndMessage) {
		this.name = name;
		this.topicNumber = topicNumber;
		this.talkNumber = talkNumber;
		this.notifyStart = notifyStart;
		this.notifyStartMessage = notifyStartMessage;
		this.notifyEnd = notifyEnd;
		this.notifyEndMessage = notifyEndMessage;
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		final BuildWrapperDelegate delegate = new BuildWrapperDelegate(name, Long.valueOf(topicNumber), Long.valueOf(talkNumber), listener, build);
		delegate.notifyStart(notifyStart, notifyStartMessage);

		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				delegate.notifyEnd(notifyEnd, notifyEndMessage);
				return true;
			}
		};
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {

		public DescriptorImpl() {
			super(TypetalkBuildWrapper.class);
			load();
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Notify Typetalk when the build starts/ends";
		}
	}

}
