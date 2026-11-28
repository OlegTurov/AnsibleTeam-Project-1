package com.example.service;

import com.example.config.BotConfig;
import com.example.exception.NotQuantityMessageException;
import com.example.response.MenuResponse;
import com.example.statehandler.ChatState;
import com.example.statehandler.SessionTracker;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class TelegramBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final BotConfig botConfig;
    private final KeyboardFormatter keyboardFormatter;
    private final TelegramClient telegramClient;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    private final Map<Long, SessionTracker> sessions = new HashMap<>();
    private final DatabaseService dbService;

    public TelegramBotService(BotConfig botConfig, KeyboardFormatter keyboardFormatter, DatabaseService dbService) {
        this.botConfig = botConfig;
        this.telegramClient = new OkHttpTelegramClient(botConfig.token());
        this.keyboardFormatter = keyboardFormatter;
        this.dbService = dbService;
    }

    @Override
    public String getBotToken() {
        return botConfig.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                processMessage(update);
            } else if (update.hasCallbackQuery()) {
                processCallback(update);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (NotQuantityMessageException e) {
            log.info("User wrote bad message");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processCallback(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();
        String callbackQueryId = callbackQuery.getId();
        logger.info("User {} pressed {}", callbackQueryId, callbackData);
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        EditMessageText editMessage = null;
        switch (callbackData) {
            case "MENU" -> {
                MenuResponse menu = dbService.getMenu();

                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Choose pizza:")
                        .replyMarkup(keyboardFormatter.createMenuKeyboard(menu))
                        .build();
            }

            case "ORDER" -> {
                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        //.text(dbService.printCurrentOrder(chatId))
                        .replyMarkup(keyboardFormatter.createOrderKeyboard())
                        .build();
            }

            case "MAIN-EXIT" -> {
                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Choose the option:")
                        .replyMarkup(keyboardFormatter.createMainKeyboard())
                        .build();
            }

            case "CHECK" -> {
                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        //.text(dbService.printCurrentOrder(chatId))
                        .replyMarkup(keyboardFormatter.createOrderKeyboard())
                        .build();
            }

            case "CLEAR" -> {
                // Очистка заказа
                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Order cleared. Choose the option:")
                        .replyMarkup(keyboardFormatter.createOrderKeyboard())
                        .build();
            }

            default -> {
                SessionTracker session = sessions.get(chatId);

                if (session == null) {
                    break;
                }

                if (ChatState.QUANTITY.equals(session.getState())) {
                    int quantity = Integer.parseInt(callbackData);

                    session.getOrder().put(session.getLastProduct(), quantity);
                    session.setState(ChatState.MENU);

                } else if (callbackData.startsWith("PRODUCT_")) {
                    String productName = callbackData.substring("PRODUCT_".length());

                    session.getOrder().putIfAbsent(productName, 0);
                    session.setLastProduct(productName);
                    session.setState(ChatState.QUANTITY);
                }
            }
        }
        try {
            telegramClient.execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(Update update) throws TelegramApiException, NotQuantityMessageException {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        SendMessage sendMessage;
        logger.info("User {} writes {}", chatId, text);
        SessionTracker sessionTracker;
        if (sessions.containsKey(chatId)) {
            DeleteMessage deleteMessage = DeleteMessage.builder()
                    .messageId(update.getMessage().getMessageId())
                    .chatId(chatId)
                    .build();
            telegramClient.execute(deleteMessage);
            throw new NotQuantityMessageException();
        } else {
            sessionTracker = new SessionTracker();
            sessions.put(chatId, sessionTracker);
            sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text("Choose the option:")
                    .replyMarkup(keyboardFormatter.createMainKeyboard())
                    .build();

            if (ChatState.QUANTITY.equals(sessionTracker.getState())) {
                try {
                    int quantity = Integer.parseInt(text);
                    if (0 < quantity && quantity < 21) {
                        sessionTracker.getOrder().replace(sessionTracker.getLastProduct(), quantity);
                        sessionTracker.setState(ChatState.MENU);
                        sendMessage = SendMessage.builder()
                                .chatId(chatId)
                                .text("Choose the pizza:")
                                .replyMarkup(keyboardFormatter.createMenuKeyboard(dbService.getMenu()))
                                .build();
                    } else {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException exception) {
                    keyboardFormatter.createQuantity("Type the number from 1 to 20");
                }
            }
            telegramClient.execute(sendMessage);
        }
    }
    
}
