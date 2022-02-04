package hh.slackbot.Slackbot;

import java.io.IOException;
import java.util.List;

import com.slack.api.Slack;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.methods.request.usergroups.UsergroupsCreateRequest;
import com.slack.api.methods.request.usergroups.UsergroupsListRequest;
import com.slack.api.methods.response.usergroups.UsergroupsCreateResponse;
import com.slack.api.methods.response.usergroups.UsergroupsListResponse;
import com.slack.api.model.Usergroup;
import com.slack.api.model.event.AppMentionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hh.slackbot.Slackbot.utils.SlackTemplateUtils;

@Configuration
public class SlackApp {

    private Slack slack = Slack.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(SlackApp.class);

    @Bean
    public App initSlackApp() {
        App app = new App();
        app.command("/test", (req, ctx) -> testCommandResponse(req, ctx));

        app.blockAction("button", (req, ctx) -> blockResponse(req, ctx));

        app.command("/create", (req, ctx) -> createGroupResponse(req, ctx));

        app.command("/groups", UsergroupHandler::handleUsergroupCommand);

        app.event(AppMentionEvent.class, (req, ctx) -> mentionResponse(req, ctx));

        return app;
    }

    public Response createGroupResponse(SlashCommandRequest req, SlashCommandContext ctx)
            throws IOException, SlackApiException {
        SlashCommandPayload payload = req.getPayload();
        String[] params = payload.getText().split(" ");
        String groupName = null;
        if (params.length > 0) {
            groupName = params[0];
        }
        for (String s : params) {
            logger.info(s);
        }
        UsergroupsListResponse listResp = slack.methods().usergroupsList(
                UsergroupsListRequest.builder()
                        .token(System.getenv("SLACK_BOT_TOKEN"))
                        .build());
        List<Usergroup> usergroups = listResp.getUsergroups();
        logger.info("User Groups");
        Boolean groupFound = false;
        for (Usergroup usergroup : usergroups) {
            if (usergroup.getName().equals(groupName))
                groupFound = true;
            logger.info(usergroup.getName() + " : " + usergroup.getId());
        }
        if (!groupFound) {
            UsergroupsCreateResponse resp = slack.methods().usergroupsCreate(
                    UsergroupsCreateRequest
                            .builder()
                            .token(System.getenv("SLACK_BOT_TOKEN"))
                            .name(groupName)
                            .build());
            if (resp.isOk()) {
                logger.info(resp.getUsergroup().toString());
            } else {
                logger.warn(resp.getError());
            }
        }
        return ctx.ack();
    }

    public Response testCommandResponse(SlashCommandRequest req, SlashCommandContext ctx)
            throws IOException, SlackApiException {
        logger.warn("-------------------");
        logger.info(req.getHeaders().toString());
        logger.info(req.getPayload().toString());
        logger.warn("-------------------");
        String command = req.getPayload().getText();
        logger.info("Test command parameters were: " + command);
        slack.methods().chatPostEphemeral(
                ChatPostEphemeralRequest.builder()
                        .token(System.getenv("SLACK_BOT_TOKEN"))
                        .channel(req.getPayload().getChannelId())
                        .user(req.getPayload().getUserId())
                        .blocksAsString(SlackTemplateUtils.getTemplate("test-payload.json"))
                        .text("Test button")
                        .build());
        return ctx.ack(":wave: This is a test command");
    }

    public Response blockResponse(BlockActionRequest req, ActionContext ctx)
            throws IOException, SlackApiException {
        ctx.respond("Button was pressed");
        return ctx.ack();
    }

    public static Response mentionResponse(EventsApiPayload<AppMentionEvent> req, EventContext ctx)
            throws IOException, SlackApiException {
        ctx.say("Greetings :wave:");
        System.out.println(req.getEvent());
        System.out.println(ctx);
        return ctx.ack();
    }
}
