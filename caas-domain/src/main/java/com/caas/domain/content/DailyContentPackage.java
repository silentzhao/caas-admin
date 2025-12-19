package com.caas.domain.content;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 每日内容包实体，用于聚合当天的选题与内容产物。
 */
public class DailyContentPackage {

    private String id;
    private LocalDate businessDate;
    private String name;
    private String region;
    private String language;
    private String ownerId;
    private String status;
    private List<String> hotTopicIds;
    private List<String> articleDraftIds;
    private List<String> videoScriptIds;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getHotTopicIds() {
        return hotTopicIds;
    }

    public void setHotTopicIds(List<String> hotTopicIds) {
        this.hotTopicIds = hotTopicIds;
    }

    public List<String> getArticleDraftIds() {
        return articleDraftIds;
    }

    public void setArticleDraftIds(List<String> articleDraftIds) {
        this.articleDraftIds = articleDraftIds;
    }

    public List<String> getVideoScriptIds() {
        return videoScriptIds;
    }

    public void setVideoScriptIds(List<String> videoScriptIds) {
        this.videoScriptIds = videoScriptIds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}
