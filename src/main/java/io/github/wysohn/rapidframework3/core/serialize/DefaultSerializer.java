package io.github.wysohn.rapidframework3.core.serialize;

import copy.com.google.gson.*;
import io.github.wysohn.rapidframework3.interfaces.serialize.CustomAdapter;

import java.lang.reflect.Type;

public class DefaultSerializer<T> implements CustomAdapter<T> {
    private static final Gson gson = new Gson();

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return gson.fromJson(json, typeOfT);
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return gson.toJsonTree(src, typeOfSrc);
    }
}
