package org.jenkinsci.plugins.typetalk.pipeline;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.typetalk.delegate.NotifyDelegate;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class TypetalkSendStep extends AbstractStepImpl {

    private final @Nonnull String name;
    private final @Nonnull Long topicId;
    private final @Nonnull Long talkId;

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Long getTopicId() {
        return topicId;
    }

    @Nonnull
    public Long getTalkId() {
        return talkId;
    }

    @DataBoundConstructor
    public TypetalkSendStep(@Nonnull String name, @Nonnull Long topicId, @Nonnull Long talkId) {
        this.name = name;
        this.topicId = topicId;
        this.talkId = talkId;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(TypetalkSendStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "typetalkSend";
        }

        @Override
        public String getDisplayName() {
            return "Notify Typetalk when the build fails";
        }
    }

    public static class TypetalkSendStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        transient TypetalkSendStep step;

        @StepContextParameter
        transient TaskListener listener;

        @StepContextParameter
        transient Run run;

        @Override
        protected Void run() throws Exception {
            new NotifyDelegate(step.name, step.topicId, step.talkId, listener, run).notifyResult();
            return null;
        }

    }

}
