package com.example.myllmapp.model;

import java.util.List;

public class OpenAIResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    public String getId() {
        return id;
    }

    public String getObject() {
        return object;
    }

    public long getCreated() {
        return created;
    }

    public String getModel() {
        return model;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public Usage getUsage() {
        return usage;
    }

    public static class Choice {
        private int index;
        private OpenAIMessage message;
        private String finish_reason;

        public int getIndex() {
            return index;
        }

        public OpenAIMessage getMessage() {
            return message;
        }

        public String getFinish_reason() {
            return finish_reason;
        }
    }

    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;

        public int getPrompt_tokens() {
            return prompt_tokens;
        }

        public int getCompletion_tokens() {
            return completion_tokens;
        }

        public int getTotal_tokens() {
            return total_tokens;
        }
    }
}