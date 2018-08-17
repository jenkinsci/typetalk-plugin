package org.jenkinsci.plugins.typetalk.pipeline;


import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.typetalk.delegate.BuildWrapperDelegate;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class TypetalkBuildWrapperStep extends AbstractStepImpl {

    private final @Nonnull String name;
    private final @Nonnull Long topicId;
    private final @Nonnull Long talkId;
    private boolean notifyStart;
    private String notifyStartMessage;
    private boolean notifyEnd;
    private String notifyEndMessage;

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

    public boolean isNotifyStart() {
        return notifyStart;
    }

    @DataBoundSetter
    public void setNotifyStart(boolean notifyStart) {
        this.notifyStart = notifyStart;
    }

    public String getNotifyStartMessage() {
        return notifyStartMessage;
    }

    @DataBoundSetter
    public void setNotifyStartMessage(String notifyStartMessage) {
        this.notifyStartMessage = notifyStartMessage;
    }

    public boolean isNotifyEnd() {
        return notifyEnd;
    }

    @DataBoundSetter
    public void setNotifyEnd(boolean notifyEnd) {
        this.notifyEnd = notifyEnd;
    }

    public String getNotifyEndMessage() {
        return notifyEndMessage;
    }

    @DataBoundSetter
    public void setNotifyEndMessage(String notifyEndMessage) {
        this.notifyEndMessage = notifyEndMessage;
    }

    @DataBoundConstructor
    public TypetalkBuildWrapperStep(@Nonnull String name, @Nonnull Long topicId, @Nonnull Long talkId) {
        this.name = name;
        this.topicId = topicId;
        this.talkId = talkId;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(TypetalkBuildWrapperStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "withTypetalk";
        }

        @Override
        public String getDisplayName() {
            return "Notify Typetalk when the build starts/ends";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }

    }

    public static class TypetalkBuildWrapperStepExecution extends AbstractStepExecutionImpl {

        private static final long serialVersionUID = 1L;

        @Inject
        transient TypetalkBuildWrapperStep step;

        @StepContextParameter
        transient TaskListener listener;

        @StepContextParameter
        transient Run run;

        @Override
        public boolean start() throws Exception {
            getContext().newBodyInvoker().withCallback(new Callback(step, listener, run)).start();
            return false;
        }

        @Override
        public void stop(@Nonnull Throwable throwable) throws Exception {
            // Do nothing
        }
    }

    public static class Callback extends BodyExecutionCallback {

        private static final long serialVersionUID = 1L;

        private transient final TypetalkBuildWrapperStep step;

        private transient final BuildWrapperDelegate delegate;

        Callback(TypetalkBuildWrapperStep step, TaskListener listener, Run run) {
            this.step = step;
            delegate = new BuildWrapperDelegate(step.name, step.topicId, step.talkId, listener, run);
        }

        @Override
        public void onStart(StepContext context) {
            try {
                delegate.notifyStart(step.notifyStart, step.notifyStartMessage);
            } catch (Exception x) {
                context.onFailure(x);
            }
        }

        @Override
        public void onSuccess(StepContext context, Object result) {
            try {
                delegate.notifyEnd(step.notifyEnd, step.notifyEndMessage);
                context.onSuccess(result);
            } catch (Exception x) {
                context.onFailure(x);
            }
        }

        @Override
        public void onFailure(StepContext context, Throwable t) {
            try {
                context.onFailure(t);
            } catch (Exception x) {
                context.onFailure(x);
            }
        }
    }

}
