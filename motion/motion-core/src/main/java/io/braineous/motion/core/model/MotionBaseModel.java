package io.braineous.motion.core.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class MotionBaseModel {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    //----------------------------------------------------------
    //---------------------------------------------
    public String toJson() {
        return GSON.toJson(this);
    }

    //---------------------------------------------
    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null) {
            throw new IllegalArgumentException("json cannot be null");
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("json cannot be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return GSON.fromJson(json, type);
    }
}
