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
import java.util.stream.Collectors;

public class UUIDFetcher {

    private static final HashMap<String, String> UUID_CACHE = new HashMap();

    public static String getUUID( String username ) {
        if ( UUID_CACHE.containsKey( username ) )
            return UUID_CACHE.get( username );

        try {
            URL url = new URL( "https://api.mojang.com/users/profiles/minecraft/" + username );
            InputStream stream = url.openStream();

            InputStreamReader streamReader = new InputStreamReader( stream );
            BufferedReader bufferedReader = new BufferedReader( streamReader );

            String result = bufferedReader.lines().collect( Collectors.joining() );
            JsonElement jsonElement = new JsonParser().parse( result );
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String id = jsonObject.get( "id" ).toString();
            id = id.substring( 1 );
            id = id.substring( 0, id.length() - 1 );

            StringBuilder stringBuilder = new StringBuilder( id );
            stringBuilder.insert( 8, "-" ).insert( 13, "-" ).insert( 18, "-" ).insert( 23, "-" );

            String uuid = stringBuilder.toString();
            UUID_CACHE.put( username, uuid );

            return uuid;
        } catch ( IOException | IllegalStateException ignored ) { }
        return null;
    }
}
