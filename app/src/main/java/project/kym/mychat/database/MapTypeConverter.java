package project.kym.mychat.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

import androidx.room.TypeConverter;

public class MapTypeConverter {
    @TypeConverter
    public static Map<String, String> fromString(String value) {
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        return new Gson().fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromStringMap(Map<String, String> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }
}
