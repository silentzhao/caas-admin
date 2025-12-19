package com.caas.domain.content;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频脚本实体，用于描述完整脚本与分段内容。
 */
public class VideoScript {

    private String id;
    private String hotTopicId;
    private String title;
    private String style;
    private Integer targetDurationSeconds;
    private String language;
    private String narration;
    private List<Segment> segments;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHotTopicId() {
        return hotTopicId;
    }

    public void setHotTopicId(String hotTopicId) {
        this.hotTopicId = hotTopicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Integer getTargetDurationSeconds() {
        return targetDurationSeconds;
    }

    public void setTargetDurationSeconds(Integer targetDurationSeconds) {
        this.targetDurationSeconds = targetDurationSeconds;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 视频脚本分段，包含每段的文本与视觉提示信息。
     */
    public static class Segment {

        private Integer orderIndex;
        private String type;
        private String text;
        private String visualNotes;
        private Integer durationSeconds;
        private List<String> assetUrls;
        private Integer startOffsetSeconds;

        public Integer getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(Integer orderIndex) {
            this.orderIndex = orderIndex;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getVisualNotes() {
            return visualNotes;
        }

        public void setVisualNotes(String visualNotes) {
            this.visualNotes = visualNotes;
        }

        public Integer getDurationSeconds() {
            return durationSeconds;
        }

        public void setDurationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
        }

        public List<String> getAssetUrls() {
            return assetUrls;
        }

        public void setAssetUrls(List<String> assetUrls) {
            this.assetUrls = assetUrls;
        }

        public Integer getStartOffsetSeconds() {
            return startOffsetSeconds;
        }

        public void setStartOffsetSeconds(Integer startOffsetSeconds) {
            this.startOffsetSeconds = startOffsetSeconds;
        }
    }
}
