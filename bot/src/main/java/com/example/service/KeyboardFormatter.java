package com.example.service;

import com.example.response.MenuResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyboardFormatter {

    public InlineKeyboardMarkup createMainKeyboard() {
        InlineKeyboardButton catalog = InlineKeyboardButton.builder()
                .text("Menu")
                .callbackData("MENU")
                .build();

        InlineKeyboardButton order = InlineKeyboardButton.builder()
                .text("Order")
                .callbackData("ORDER")
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(catalog);
        row.add(order);

        return InlineKeyboardMarkup.builder().keyboard(List.of(row)).build();
    }

    public InlineKeyboardMarkup createMenuKeyboard(MenuResponse menu) {

        List<String> products = menu.products();
        List<Integer> costs = menu.costs();
        List<InlineKeyboardRow> rows = new ArrayList<>();
        int i = 0;
        while (i < products.size()) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            for (int j = 0; j < 3; j++) {

                InlineKeyboardButton product = InlineKeyboardButton.builder()
                        .text("{} : {}".formatted(products.get(i), costs.get(i)))
                        .callbackData(products.get(i))
                        .build();
                row.add(product);

            }
            rows.add(row);
            i += 3;
        }

        InlineKeyboardRow row = new InlineKeyboardRow();

        InlineKeyboardButton exit = InlineKeyboardButton.builder()
                .text("Exit")
                .callbackData("MAIN-EXIT")
                .build();
        row.add(exit);

        rows.add(row);

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public InlineKeyboardMarkup createOrderKeyboard() {
        InlineKeyboardButton payment = InlineKeyboardButton.builder()
                .text("Pay")
                .callbackData("PAYMENT")
                .build();

        InlineKeyboardButton clear = InlineKeyboardButton.builder()
                .text("Clear/Cancel")
                .callbackData("CLEAR")
                .build();

        InlineKeyboardButton check = InlineKeyboardButton.builder()
                .text("Check")
                .callbackData("CHECK")
                .build();

        InlineKeyboardButton exit = InlineKeyboardButton.builder()
                .text("Exit")
                .callbackData("MAIN-EXIT")
                .build();

        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(payment);
        row1.add(check);

        InlineKeyboardRow row2 = new InlineKeyboardRow();
        row2.add(clear);
        row2.add(exit);

        return InlineKeyboardMarkup.builder().keyboard(List.of(row1, row2)).build();
    }
}
