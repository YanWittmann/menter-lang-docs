package de.yanwittmann.menter.doc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SitemapGenerator {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final String baseUrl;
    private final List<String> urls;

    public SitemapGenerator(String baseUrl, List<DocumentationPage> documentationPages, File[] additionalRootFiles) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("Base url must not be null or empty!");
        } else if (!baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl + "/";
        } else {
            this.baseUrl = baseUrl;
        }

        this.urls = generateUrls(documentationPages, additionalRootFiles);
    }

    private List<String> generateUrls(List<DocumentationPage> documentationPages, File[] additionalRootFiles) {
        final List<String> urls = new ArrayList<>();

        for (DocumentationPage documentationPage : documentationPages) {
            urls.add(baseUrl + documentationPage.getOutFileName());
        }

        for (File additionalRootFile : additionalRootFiles) {
            urls.add(baseUrl + additionalRootFile.getName());
        }

        return urls;
    }

    public String generate() {
        StringBuilder sitemap = new StringBuilder();
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (String url : urls) {
            sitemap.append("  <url>\n");
            sitemap.append("    <loc>").append(url).append("</loc>\n");
            sitemap.append("    <changefreq>monthly</changefreq>\n");
            sitemap.append("    <priority>0.8</priority>\n");
            sitemap.append("    <lastmod>").append(DATE_FORMAT.format(System.currentTimeMillis())).append("</lastmod>\n");
            sitemap.append("  </url>\n");
        }
        sitemap.append("</urlset>");
        return sitemap.toString();
    }
}
