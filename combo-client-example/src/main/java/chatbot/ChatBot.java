package chatbot;

import combo.Combo;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static chatbot.ChatBot.ChatMessage.chatMessage;
import static combo.HttpCombo.httpCombo;
import static combo.RestTemplateHttpClient.restTemplateHttpClient;
import static java.lang.String.format;
import static java.net.URI.create;

final class ChatBot {

    private static final String CHAT_TOPIC = "chat";
    private static final String NAME = "chat-bot";

    public static void main(final String[] args) {
        final Combo combo = httpCombo(restTemplateHttpClient(create("http://combo-squirrel.herokuapp.com")));

        combo.facts(CHAT_TOPIC, ChatMessage.class)
                .filter(ignoringMyself())
                .forEach(replyWithGreeting(combo));
    }

    private static Consumer<ChatMessage> replyWithGreeting(final Combo combo) {
        return message -> combo.publishFact(CHAT_TOPIC, chatMessage(NAME, format("Hello %s, you said '%s'", message.getWho(), message.getSays())));
    }

    private static Predicate<ChatMessage> ignoringMyself() {
        return message -> !NAME.equals(message.getWho());
    }

    static final class ChatMessage {

        private final String who;
        private final String says;

        ChatMessage(final String who, final String says) {
            this.who = who;
            this.says = says;
        }

        String getWho() {
            return who;
        }

        String getSays() {
            return says;
        }

        static ChatMessage chatMessage(final String who, final String says) {
            return new ChatMessage(who, says);
        }
    }
}
