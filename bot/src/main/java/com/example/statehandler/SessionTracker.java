package com.example.statehandler;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class SessionTracker {
    Map<String, Integer> order = new HashMap<>();
    ChatState state = ChatState.MENU;
    String lastProduct;
}
