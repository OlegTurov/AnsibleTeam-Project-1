package com.example.service;

import com.example.config.BotConfig;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
@Log4j2
public class TelegramBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final BotConfig botConfig;
    private final TelegramClient telegramClient;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

    public TelegramBotService(BotConfig botConfig) {
        this.botConfig = botConfig;
        this.telegramClient = new OkHttpTelegramClient(botConfig.token());
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
        if (update.hasMessage()) {

            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            logger.info("User {} writes {}",chatId, text);
            if (update.getMessage().hasText()) {
                SendMessage message = SendMessage.builder()
                        .chatId(chatId.toString())
                        .text(text)
                        .replyMarkup(createMainMenuKeyboard())
                        .build();
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                if (update.hasCallbackQuery()) {
                    String callbackQuery = update.getCallbackQuery().getData();
                    switch (callbackQuery) {
                        case "MENU":
                            SendMessage message = SendMessage.builder()
                                    .chatId(chatId.toString())
                                    .text(text)
                                    .replyMarkup(createMenuKeyboard())
                                    .build();
                    }
                }
            }
        }
    }

    private InlineKeyboardMarkup createMainMenuKeyboard() {
        InlineKeyboardButton catalog = InlineKeyboardButton.builder()
                .text("Каталог")
                .callbackData("MENU")
                .build();

        InlineKeyboardButton order = InlineKeyboardButton.builder()
                .text("Заказ")
                .callbackData("ORDER")
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(catalog);
        row.add(order);

        return InlineKeyboardMarkup.builder().keyboard(List.of(row)).build();
    }

    private InlineKeyboardMarkup createMenuKeyboard() {
        InlineKeyboardButton catalog = InlineKeyboardButton.builder()
                .text("Пицца 1")
                .callbackData("Pizza1")
                .build();

        InlineKeyboardButton order = InlineKeyboardButton.builder()
                .text("Pizza 2")
                .callbackData("Pizza2")
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(catalog);
        row.add(order);

        return InlineKeyboardMarkup.builder().keyboard(List.of(row)).build();
    }
}
