package com.example.myllmapp.model;

import java.util.List;

public class OpenAIRequest {
    private String model;
    private List<OpenAIMessage> messages;
    private double temperature;
    private int max_tokens;

    public OpenAIRequest(String model, List<OpenAIMessage> messages, double temperature, int max_tokens) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.max_tokens = max_tokens;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<OpenAIMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<OpenAIMessage> messages) {
        this.messages = messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }
}