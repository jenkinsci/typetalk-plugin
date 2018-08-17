package org.jenkinsci.plugins.typetalk.delegate;

import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.typetalk.api.Typetalk;
import org.jenkinsci.plugins.typetalk.support.Emoji;
import org.jenkinsci.plugins.typetalk.support.TypetalkMessage;

import java.io.IOException;

public class BuildWrapperDelegate {

    private final String name;

    private final Long topicId;

    private final Long talkId;

    private final TaskListener listener;

    private final Run run;

    public BuildWrapperDelegate(String name, Long topicId, Long talkId, TaskListener listener, Run run) {
        this.name = name;
        this.topicId = topicId;
        this.talkId = talkId;
        this.listener = listener;
        this.run = run;
    }

    public void notifyStart(boolean notifyStart, String notifyStartMessage) throws IOException, InterruptedException {
        if (notifyStart) {
            listener.getLogger().println("Notifying build start to Typetalk...");

            String message;
            if (StringUtils.isBlank(notifyStartMessage)) {
                TypetalkMessage typetalkMessage = new TypetalkMessage(Emoji.LOUDSPEAKER, "Build start");
                message = typetalkMessage.buildMessageWithBuild(run);
            } else {
                message = run.getEnvironment(listener).expand(notifyStartMessage);
            }

            Typetalk.createFromName(name).postMessage(topicId, message, talkId);
        }
    }

    public void notifyEnd(boolean notifyEnd, String notifyEndMessage) throws IOException, InterruptedException {
        if (notifyEnd && isSuccessBuild(run)) {
            listener.getLogger().println("Notifying build end to Typetalk...");

            String message;
            if (StringUtils.isBlank(notifyEndMessage)) {
                TypetalkMessage typetalkMessage = new TypetalkMessage(Emoji.MEGA, "Build end");
                message = typetalkMessage.buildMessageWithBuild(run);
            } else {
                message = run.getEnvironment(listener).expand(notifyEndMessage);
            }

            Typetalk.createFromName(name).postMessage(topicId, message, talkId);
        }
    }

    private boolean isSuccessBuild(Run run) {
        // When there is nothing failure (equals success), getResult hasn't been set yet.
        return run.getResult() == null || run.getResult().equals(Result.SUCCESS);
    }

}
