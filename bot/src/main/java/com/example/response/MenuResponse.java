package com.example.response;

import java.util.List;

public record MenuResponse (List<String> products, List<Integer> costs) {

}
