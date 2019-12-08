package de.tutorialwork.professionalbans.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

public class UUIDFetcher
{
    private static HashMap<String, String> uuidCache = new HashMap();

    public static String getUUID(String username)
    {
        if (uuidCache.containsKey(username)) {
            return (String)uuidCache.get(username);
        }
        try
        {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            InputStream stream = url.openStream();
            InputStreamReader inr = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(inr);
            String s = null;
            StringBuilder sb = new StringBuilder();
            while ((s = reader.readLine()) != null) {
                sb.append(s);
            }
            String result = sb.toString();
            JsonElement element = new JsonParser().parse(result);
            JsonObject obj = element.getAsJsonObject();
            String api = obj.get("id").toString();
            api = api.substring(1);
            api = api.substring(0, api.length() - 1);
            StringBuffer sbu = new StringBuffer(api);
            sbu.insert(8, "-").insert(13, "-").insert(18, "-").insert(23, "-");
            String uuid = sbu.toString();
            uuidCache.put(username, uuid);
            return uuid;
        }
        catch (IOException|IllegalStateException localIOException) {}
        return null;
    }
}