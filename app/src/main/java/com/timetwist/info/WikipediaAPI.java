package com.timetwist.info;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class WikipediaAPI {
    private static final String ARTICLE_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/%s";

    public static String fetchArticle(String title) {
        try {
            URL url = new URL(String.format(ARTICLE_URL, title.replaceAll(" ", "_")));
            InputStreamReader reader = new InputStreamReader(url.openStream());
            Article article = new Gson().fromJson(reader, Article.class);
            return article.extract;
        } catch (IOException e) {
            return "Failed to fetch article: " + e.getMessage();
        }
    }

    private static class Article {
        private String extract;
    }
}

