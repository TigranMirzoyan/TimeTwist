package com.timetwist.info;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class WikipediaAPI {
    private static final String ARTICLE_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/%s";

    public static void fetchArticle(String title, Consumer<String> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                URL url = new URL(String.format(ARTICLE_URL, title.replaceAll(" ", "_")));
                InputStreamReader reader = new InputStreamReader(url.openStream());
                Article article = new Gson().fromJson(reader, Article.class);
                String result = article.extract;

                handler.post(() -> callback.accept(result));
            } catch (IOException e) {
                String errorResult = "Failed to fetch article: " + e.getMessage();
                handler.post(() -> callback.accept(errorResult));
            }
        });

        executor.shutdown();
    }

    private static class Article {
        private String extract;
    }
}

