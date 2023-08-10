package de.base2code.potions.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.base2code.potions.Potions;
import org.bukkit.Bukkit;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.google.common.net.HttpHeaders.USER_AGENT;

public class UUIDFetcher {

    /**
     * Date when name changes were introduced
     *
     */
    public static final long FEBRUARY_2015 = 1422748800000L;

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
    private static final String NAME_URL = "https://sessionserver.mojang.com"
            + "/session/minecraft/profile/";
    private static final Cache<String, UUID> uuidCache = CacheBuilder.newBuilder().expireAfterAccess(30L, TimeUnit.MINUTES).build();
    private static final Cache<UUID, String> nameCache = CacheBuilder.newBuilder().expireAfterAccess(30L, TimeUnit.MINUTES).build();

    private static final ExecutorService pool = Executors.newCachedThreadPool();

    private String name;
    private UUID id;

    /**
     * Fetches the uuid synchronously and returns it
     *
     * @param name The name
     * @return The uuid
     */
    public static UUID getUUID(String name) {
        if (Bukkit.getPlayer(name) != null) {
            return Bukkit.getPlayer(name).getUniqueId();
        }

        // Make http get request
        // https://api.mojang.com/users/profiles/minecraft/
        try {
            return sendHttpGETRequest(name);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static UUID sendHttpGETRequest(String name) throws IOException {
        URL obj = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();

            JSONObject jsonObject = new JSONObject(response.toString());
            String uuid = jsonObject.getString("id");
            uuid = uuid.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
            );
            return UUID.fromString(uuid);
        } else{
            Potions.getInstance().getLogger().info("Could not get UUID from Mojang API: " + name);
        }

        return null;
    }

    /**
     * Fetches the name asynchronously and passes it to the consumer
     *
     * @param uuid   The uuid
     * @param action Do what you want to do with the name her
     */
    public static void getName(UUID uuid, Consumer<String> action) {
        pool.execute(() -> action.accept(getName(uuid)));
    }

    /**
     * Fetches the name synchronously and returns it
     *
     * @param uuid The uuid
     * @return The name
     */
    public static String getName(UUID uuid) {
        return getName(uuid.toString());
    }

    public static String getName(String uuid) {
        uuid = uuid.replace("-", "");
        String output = callURL(NAME_URL + uuid);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            if (output.charAt(i) == 'n' && output.charAt(i + 1) == 'a'
                    && output.charAt(i + 2) == 'm'
                    && output.charAt(i + 3) == 'e') {
                for (int k = i + 9; k < 20000; k++) {
                    char curr = output.charAt(k);
                    if (curr != '"') {
                        result.append(curr);
                    } else {
                        break;
                    }
                }
                break;
            }
        }
        return result.toString();
    }

    private static String callURL(String urlStr) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in;
        try {
            URL url = new URL(urlStr);
            urlConn = url.openConnection();
            if (urlConn != null) {
                urlConn.setReadTimeout(60 * 1000);
            }
            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(),
                        Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                int cp;
                while ((cp = bufferedReader.read()) != -1) {
                    sb.append((char) cp);
                }
                bufferedReader.close();
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String parseJSON(final String json, final String key) {
        final JsonElement element = new JsonParser().parse(json);
        if (element instanceof JsonNull) {
            return null;
        }
        final JsonElement obj = ((JsonObject) element).get(key);
        return (obj != null) ? obj.toString().replaceAll("\"", "") : null;
    }
}



