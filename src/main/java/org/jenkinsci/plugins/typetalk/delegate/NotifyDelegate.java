package org.jenkinsci.plugins.typetalk.delegate;

import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.typetalk.api.Typetalk;
import org.jenkinsci.plugins.typetalk.support.ResultSupport;
import org.jenkinsci.plugins.typetalk.support.TypetalkMessage;

import java.io.IOException;

public class NotifyDelegate {

    private final String name;

    private final Long topicId;

    private final Long talkId;

    private final TaskListener listener;

    private final Run run;

    public NotifyDelegate(String name, Long topicId, Long talkId, TaskListener listener, Run run) {
        this.name = name;
        this.topicId = topicId;
        this.talkId = talkId;
        this.listener = listener;
        this.run = run;
    }

    public void notifyResult() throws IOException {
        ResultSupport resultSupport = new ResultSupport();
        if (resultSupport.isSuccessFromSuccess(run)) {
            return;
        }

        listener.getLogger().println("Notifying build result to Typetalk...");

        TypetalkMessage typetalkMessage = resultSupport.convertBuildToMessage(run);
        String message = typetalkMessage.buildMessageWithBuild(run);

        Typetalk.createFromName(name).postMessage(topicId, message, talkId);
    }

}
