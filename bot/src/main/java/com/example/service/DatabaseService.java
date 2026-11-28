package com.example.service;

import com.example.response.MenuResponse;
import com.example.response.PositionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseService {
    public MenuResponse getMenu() {
        return new MenuResponse(List.of(
                new PositionResponse("Margarita", 500),
                new PositionResponse("Piperoni", 100),
                new PositionResponse("Four Cheeses", 100),
                new PositionResponse("Diablo", 200)
                ));
    }
}
