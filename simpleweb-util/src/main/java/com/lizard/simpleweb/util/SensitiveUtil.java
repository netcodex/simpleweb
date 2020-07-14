package com.lizard.simpleweb.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.regex.Pattern;

/**
 * 描述：
 *
 * @author x
 * @since 2020-07-02 21:33
 */
public class SensitiveUtil {

    /**
     * 过滤字段
     * 
     * @param t
     *            普通JO
     * @param excludeFieldNames
     *            过滤字段
     * @return 过滤字段后的字符串
     */
    public static <T> String sensitiveFieldFilter(T t, String[] excludeFieldNames) {
        return ReflectionToStringBuilder.toStringExclude(t, excludeFieldNames);
    }

    public static String maskFieldString() {
        char singleQuote = 39;
        char asterisk = 42;
        return StringUtils.wrap(StringUtils.repeat('*', 10), '\'');
    }

    public static final String field = "password";

    public static boolean hasSensitiveItem(String str) {
        Pattern pattern = Pattern.compile("[]");
        return false;
    }

    // 遍历嵌套JSON
    public static void jsonFieldFilter(Object objJson) {
        if (objJson instanceof JSONArray) {
            JSONArray objArray = (JSONArray)objJson;
            for (Object o : objArray) {
                jsonFieldFilter(o);
            }
        } else if (objJson instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject)objJson;
            for (String key : jsonObject.keySet()) {
                Object object = jsonObject.get(key);
                try {
                    Object json = JSON.toJSON(object);
                    if (json instanceof JSONArray) {
                        JSONArray objArray = (JSONArray)json;
                        jsonFieldFilter(objArray);
                    } else if (json instanceof JSONObject) {
                        jsonFieldFilter(json);
                        jsonObject.put(key, json);
                    }
                    if (json instanceof String && field.equals(key)) {
                        jsonObject.replace(key, object, "******");
                    }
                } catch (JSONException ignored) {

                }

            }
        }
    }
}