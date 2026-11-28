package com.example.service;

import com.example.config.BotConfig;
import com.example.response.MenuResponse;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class TelegramBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final BotConfig botConfig;
    private final KeyboardFormatter keyboardFormatter;
    private final TelegramClient telegramClient;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

    public TelegramBotService(BotConfig botConfig, KeyboardFormatter keyboardFormatter) {
        this.botConfig = botConfig;
        this.telegramClient = new OkHttpTelegramClient(botConfig.token());
        this.keyboardFormatter = keyboardFormatter;
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            processMessage(update);
        } else if (update.hasCallbackQuery()) {
            processCallback(update);
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
                //Поход к сервису GO, возврат в виде MENU (AL, AL)
                MenuResponse menu = dbService.getMenu();
                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Выберите пиццу:")
                        .replyMarkup(keyboardFormatter.createMenuKeyboard(menu))
                        .build();
            }
            case "ORDER":
                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        //здесь нужно вывести форматированный заказ
                        .text(printCurrentOrder())
                        .replyMarkup(keyboardFormatter.createOrderKeyboard())
                        .build();
            case "MAIN-EXIT":
                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .replyMarkup(keyboardFormatter.createMainKeyboard())
                        .build();

            case "CHECK":
                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        //здесь нужно вывести форматированный заказ
                        .text(printCurrentOrder())
                        .replyMarkup(keyboardFormatter.createOrderKeyboard())
                        .build();
            case "CLEAR":
                //Логика с проверкой состояния заказа, возможным удалением из базы
                editMessage = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        //здесь нужно вывести форматированный заказ
                        .text(printCurrentOrder())
                        .replyMarkup(keyboardFormatter.createOrderKeyboard())
                        .build();
        }
        try {
            telegramClient.execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        logger.info("User {} writes {}",chatId, text);
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите пункт меню:")
                .replyMarkup(keyboardFormatter.createMainKeyboard())
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
}
