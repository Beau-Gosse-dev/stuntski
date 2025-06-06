package com.deaddropgames.stuntmountain.web;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.deaddropgames.stuntmountain.level.Level;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

public class LevelRepository {

    private static final String LOG = "LevelRepository";

    private static final String baseUrl = "https://deaddropgames.com";
    private static final String getLevelListPath = "stuntski/api/levels/";
    private static final String getLevelListTopRatedPath = "stuntski/api/levels/toprated/";
    private static final String getLevelPath = "stuntski/api/levels/%d/";
    private static final String initTokenPath = "auth/token/";
    private static final String levelVotePath = "stuntski/api/levels/vote/";

    private static Json json;

    // cache our web requests for up to 5 minutes
    private static Cache cache = new Cache(5*60*1000);

    // cache the login token
    private static String token;

    public static LevelSummary getLevelList(String path) throws IOException, URISyntaxException {

        initJson();

        // if path is null use the default, otherwise assume the full path is passed through
        URL url;
        if (path == null) {

            url = createUrl(getLevelListPath);
        } else {

            url = new URL(path);
        }
        Gdx.app.debug(LOG, url.toString());

        return getLevelSummary(url);
    }

    public static LevelSummary getTopRatedList(String path) throws IOException, URISyntaxException {

        initJson();

        // if path is null use the default, otherwise assume the full path is passed through
        URL url;
        if (path == null) {

            url = createUrl(getLevelListTopRatedPath);
        } else {

            url = new URL(path);
        }
        Gdx.app.debug(LOG, url.toString());

        return getLevelSummary(url);
    }

    private static LevelSummary getLevelSummary(final URL url) throws IOException {

        LevelSummary levelSummary = (LevelSummary)cache.getItem(url.toString());
        if (levelSummary == null) {

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            configureConnection(conn);

            if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                levelSummary = json.fromJson(LevelSummary.class, conn.getInputStream());
                cache.addItem(url.toString(), levelSummary);
            }
        }

        return levelSummary;
    }

    public static Level getLevel(long id) throws IOException, URISyntaxException {

        initJson();

        URL url = createUrl(String.format(Locale.getDefault(), getLevelPath, id));
        Gdx.app.debug(LOG, url.toString());

        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        configureConnection(conn);

        Level level = (Level)cache.getItem(url.toString());
        if (level == null) {

            if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                level = json.fromJson(Level.class, conn.getInputStream());
                cache.addItem(url.toString(), level);
            }
        }

        return level;
    }

    public static boolean hasToken() {

        return token != null;
    }

    private static class AuthToken {

        String token = null;
    }

    private static class LoginData {

        String username;
        String password;
    }

    public static boolean initToken(final String username, final String password) throws IOException, URISyntaxException {

        initJson();

        token = null;

        URL url = createUrl(initTokenPath);
        Gdx.app.debug(LOG, url.toString());

        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        configureConnection(conn);
        conn.setDoOutput(true);

        // set the body data as JSON
        LoginData data = new LoginData();
        data.username = username;
        data.password = password;

        OutputStream wr = conn.getOutputStream();
        wr.write(json.toJson(data, LoginData.class).getBytes("UTF-8"));
        wr.flush();
        wr.close();

        if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {

            token = json.fromJson(AuthToken.class, conn.getInputStream()).token;
        }

        return token != null;
    }

    private static class LevelVoteData {

        long level;
        int vote;
    }

    public static void voteForLevel(long levelId, int vote) throws IOException, URISyntaxException {

        initJson();

        URL url = createUrl(levelVotePath);
        Gdx.app.debug(LOG, url.toString());

        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        configureConnection(conn);
        conn.setDoOutput(true);

        // set the body data as JSON
        LevelVoteData data = new LevelVoteData();
        data.level = levelId;
        data.vote = vote;

        OutputStream wr = conn.getOutputStream();
        wr.write(json.toJson(data, LevelVoteData.class).getBytes("UTF-8"));
        wr.flush();
        wr.close();

        // we should always get a 201 created for this call
        if (conn.getResponseCode() != HttpsURLConnection.HTTP_CREATED) {

            Gdx.app.error(LOG, String.format(Locale.getDefault(), "Failed to vote %d for level %d", vote, levelId));
            Gdx.app.error(LOG, String.valueOf(conn.getResponseCode()) + ": " + conn.getResponseMessage());
        }
    }

    private static URL createUrl(final String path) throws MalformedURLException {

        return new URL(new URL(baseUrl), path);
    }

    private static void initJson() {

        if (json == null) {

            json = new Json(JsonWriter.OutputType.json);
            json.setIgnoreUnknownFields(true);
        }
    }

    private static void configureConnection(HttpsURLConnection conn) throws ProtocolException {

        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Encoding", "UTF-8");

        // add authorization if we have it
        if (token != null) {

            conn.setRequestProperty("Authorization", "Token " + token);
        }

        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
    }
}
