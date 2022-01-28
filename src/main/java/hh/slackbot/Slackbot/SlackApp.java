package hh.slackbot.Slackbot;

import java.io.IOException;

import com.slack.api.Slack;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.model.event.AppMentionEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackApp {

    @Autowired
    private Slack slack;

    @Bean
    public App initSlackApp() {
        App app = new App();

        app.command("/test", (req, ctx) -> {
            String command = req.getPayload().getText();
            System.out.println(command);
            slack.methods().chatPostEphemeral(
                ChatPostEphemeralRequest.builder()
                    .token(System.getenv("SLACK_BOT_TOKEN"))
                    .channel(req.getPayload().getChannelId())
                    .user(req.getPayload().getUserId())
                    .text("Builder test")
                    .build()
            );
            return ctx.ack(":wave: This is a test command");
        });

        app.event(AppMentionEvent.class, (req, ctx) -> mentionResponse(req, ctx));

        return app;
    }

    public static Response mentionResponse(EventsApiPayload<AppMentionEvent> req, EventContext ctx)
            throws IOException, SlackApiException {
        ctx.say("Greetings :wave:");
        System.out.println(req.getEvent());
        System.out.println(ctx);
        return ctx.ack();
    }
}
