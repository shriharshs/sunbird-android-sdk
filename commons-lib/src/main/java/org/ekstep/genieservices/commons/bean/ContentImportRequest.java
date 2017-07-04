package org.ekstep.genieservices.commons.bean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class accepts,
 * - isChildContent - if the child contents are required while importing
 * - destinationFolder - where the contents are to be stored
 * - contentIds - list of contentIds to be imported
 * - correlationData - list of correlationData
 * - status - list of status i.e. "Live", "Draft"
 * - contentType - List of contentType. "Story", "Worksheet", "Collection", "Game", "TextBook", "Course", "LessonPlan".
 */
public class ContentImportRequest {

    private boolean isChildContent;
    private String destinationFolder;
    private List<String> contentIds;
    private List<CorrelationData> correlationData;
    private List<String> status;
    private List<String> contentType;

    private ContentImportRequest(boolean isChildContent, String destinationFolder, List<String> contentIds,
                                 List<CorrelationData> correlationData, List<String> status, List<String> contentType) {
        this.isChildContent = isChildContent;
        this.destinationFolder = destinationFolder;
        this.contentIds = contentIds;
        this.correlationData = correlationData;
        this.status = status;
        this.contentType = contentType;
    }

    public boolean isChildContent() {
        return isChildContent;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public List<String> getContentIds() {
        return contentIds;
    }

    public List<CorrelationData> getCorrelationData() {
        return correlationData;
    }

    public List<String> getStatus() {
        return status;
    }

    public List<String> getContentType() {
        return contentType;
    }

    public static class Builder {
        private boolean isChildContent;
        private String destinationFolder;
        private List<String> contentIds;
        private List<CorrelationData> correlationData;
        private List<String> status;
        private List<String> contentType;

        /**
         * Method to indicate that the file being imported is a child content
         */
        public Builder childContent() {
            this.isChildContent = true;
            return this;
        }

        /**
         * Destination folder where content will import.
         */
        public Builder toFolder(String toFolder) {
            if (toFolder == null) {
                throw new IllegalArgumentException("Illegal toFolder");
            }
            this.destinationFolder = toFolder;
            return this;
        }

        /**
         * List of content identifier which needs to import.
         */
        public Builder contentIds(List<String> contentIds) {
            this.contentIds = contentIds;
            return this;
        }

        /**
         * CorrelationData of content.
         */
        public Builder correlationData(List<CorrelationData> correlationData) {
            this.correlationData = correlationData;
            return this;
        }

        /**
         * List of status i.e. "Live", "Draft".
         */
        public Builder status(List<String> status) {
            this.status = status;
            return this;
        }

        /**
         * List of contentType. "Story", "Worksheet", "Collection", "Game", "TextBook", "Course", "LessonPlan".
         */
        public Builder contentType(List<String> contentType) {
            this.contentType = contentType;
            return this;
        }

        public ContentImportRequest build() {
            if (destinationFolder == null) {
                throw new IllegalStateException("To folder required.");
            }

            if (contentIds == null || contentIds.size() == 0) {
                throw new IllegalStateException("ContentIds required.");
            }

            if (status == null || status.isEmpty()) {
                this.status = Collections.singletonList("Live");
            }

            if (contentType == null || contentType.isEmpty()) {
                this.contentType = Arrays.asList("Story", "Worksheet", "Collection", "Game", "TextBook");
            }

            return new ContentImportRequest(isChildContent, destinationFolder, contentIds, correlationData, status, contentType);
        }
    }
}
