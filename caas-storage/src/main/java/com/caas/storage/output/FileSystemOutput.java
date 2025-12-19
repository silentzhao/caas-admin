package com.caas.storage.output;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caas.domain.content.ArticleDraft;
import com.caas.domain.content.VideoScript;
import com.caas.pipeline.spi.Output;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 基于文件系统的输出实现，按日期目录输出 Markdown 与视频脚本 JSON。
 */
public class FileSystemOutput implements Output<FileSystemOutput.ContentItem> {

    private static final DateTimeFormatter DATE_DIR_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_PREFIX_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final Path baseDir;

    public FileSystemOutput(Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void emit(ContentItem item) throws Exception {
        if (item == null || item.getArticle() == null || item.getVideoScript() == null) {
            throw new IllegalArgumentException("输出内容不能为空");
        }

        ArticleDraft article = item.getArticle();
        VideoScript videoScript = item.getVideoScript();

        LocalDate date = resolveDate(article).toLocalDate();
        Path dayDir = baseDir.resolve(DATE_DIR_FORMAT.format(date));
        Files.createDirectories(dayDir);

        String baseName = buildBaseName(article, date);
        Path markdownPath = dayDir.resolve(baseName + ".md");
        Path jsonPath = dayDir.resolve(baseName + ".video.json");

        String markdown = article.getBody() == null ? "" : article.getBody();
        Files.writeString(markdownPath, markdown, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        String json = buildVideoScriptJson(videoScript);
        Files.writeString(jsonPath, json, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private LocalDateTime resolveDate(ArticleDraft article) {
        if (article.getPublishedAt() != null) {
            return article.getPublishedAt();
        }
        if (article.getScheduledAt() != null) {
            return article.getScheduledAt();
        }
        if (article.getCreatedAt() != null) {
            return article.getCreatedAt();
        }
        return LocalDateTime.now();
    }

    private String buildBaseName(ArticleDraft article, LocalDate date) {
        String datePrefix = DATE_PREFIX_FORMAT.format(date);
        String titleSlug = toSlug(article.getTitle());
        String idPart = firstNonBlank(article.getId(), article.getHotTopicId());

        StringBuilder name = new StringBuilder(datePrefix);
        if (!titleSlug.isEmpty()) {
            name.append("_").append(titleSlug);
        } else {
            name.append("_article");
        }
        if (!isBlank(idPart)) {
            name.append("_").append(idPart);
        }
        return truncate(name.toString(), 80);
    }

    private String buildVideoScriptJson(VideoScript script) {
        JSONObject root = new JSONObject();
        root.put("title", script.getTitle());
        root.put("style", script.getStyle());
        root.put("target_duration_seconds", script.getTargetDurationSeconds());
        root.put("language", script.getLanguage());
        root.put("narration", script.getNarration());

        JSONArray segments = new JSONArray();
        List<VideoScript.Segment> segmentList = script.getSegments();
        if (segmentList != null) {
            for (VideoScript.Segment segment : segmentList) {
                JSONObject item = new JSONObject();
                item.put("order_index", segment.getOrderIndex());
                item.put("type", segment.getType());
                item.put("text", segment.getText());
                item.put("visual_notes", segment.getVisualNotes());
                item.put("duration_seconds", segment.getDurationSeconds());
                item.put("asset_urls", segment.getAssetUrls());
                item.put("start_offset_seconds", segment.getStartOffsetSeconds());
                segments.add(item);
            }
        }
        root.put("segments", segments);

        return JSON.toJSONString(root, true);
    }

    private String toSlug(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.toLowerCase();
        StringBuilder slug = new StringBuilder();
        boolean lastDash = false;
        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                slug.append(ch);
                lastDash = false;
            } else if (!lastDash) {
                slug.append('-');
                lastDash = true;
            }
        }
        String result = slug.toString().replaceAll("^-+|-+$", "");
        return result.replaceAll("-+", "-");
    }

    private String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first;
        }
        return second;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * 输出内容载体，包含文章与对应视频脚本。
     */
    public static class ContentItem {

        private ArticleDraft article;
        private VideoScript videoScript;

        public ContentItem() {
        }

        public ContentItem(ArticleDraft article, VideoScript videoScript) {
            this.article = article;
            this.videoScript = videoScript;
        }

        public ArticleDraft getArticle() {
            return article;
        }

        public void setArticle(ArticleDraft article) {
            this.article = article;
        }

        public VideoScript getVideoScript() {
            return videoScript;
        }

        public void setVideoScript(VideoScript videoScript) {
            this.videoScript = videoScript;
        }
    }
}
