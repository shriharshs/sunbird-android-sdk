package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.bean.enums.SearchType;

import java.io.Serializable;
import java.util.List;

/**
 * This class accepts query string, {@link List<ContentSearchFilter>}, age, grade, medium, board, audience array, channel array, sort criteria, limit and mode for searching a content with all
 * set criteria
 */
public class ContentSearchCriteria implements Serializable {

    private static final int DEFAULT_LIMIT = 100;

    private String query;
    private long limit;
    private String mode;
    private int age;
    private int grade;
    private String medium;
    private String board;
    private String[] createdBy;
    private String[] audience;
    private String[] channel;
    private String[] contentStatusArray;
    private String[] facets;
    private String[] contentTypes;
    private List<ContentSearchFilter> facetFilters;
    private List<ContentSearchFilter> impliedFilters;
    private List<ContentSortCriteria> sortCriteria;
    // 1 - indicates search, 2 - filter
    private SearchType searchType;

    private ContentSearchCriteria(String query, long limit, String mode, int age, int grade, String medium, String board, String[] createdBy,
                                  String[] audience, String[] channel, String[] contentStatusArray, String[] facets, String[] contentTypes,
                                  List<ContentSortCriteria> sortCriteria, SearchType searchType) {
        this.query = query;
        this.limit = limit;
        this.mode = mode;
        this.age = age;
        this.grade = grade;
        this.medium = medium;
        this.board = board;
        this.createdBy = createdBy;
        this.audience = audience;
        this.channel = channel;
        this.contentStatusArray = contentStatusArray;
        this.facets = facets;
        this.contentTypes = contentTypes;
        this.sortCriteria = sortCriteria;
        this.searchType = searchType;
    }

    private ContentSearchCriteria(String query, long limit, String mode, String[] facets, String[] contentTypes,
                                  List<ContentSearchFilter> facetFilters, List<ContentSearchFilter> impliedFilters,
                                  List<ContentSortCriteria> sortCriteria, SearchType searchType) {
        this.query = query;
        this.limit = limit;
        this.mode = mode;
        this.facets = facets;
        this.contentTypes = contentTypes;
        this.facetFilters = facetFilters;
        this.impliedFilters = impliedFilters;
        this.sortCriteria = sortCriteria;
        this.searchType = searchType;
    }

    public String getQuery() {
        return query;
    }

    public int getAge() {
        return age;
    }

    public int getGrade() {
        return grade;
    }

    public String getMedium() {
        return medium;
    }

    public String getBoard() {
        return board;
    }

    public String[] getCreatedBy() {
        return createdBy;
    }

    public String[] getAudience() {
        return audience;
    }

    public String[] getChannel() {
        return channel;
    }

    public String[] getContentStatusArray() {
        return contentStatusArray;
    }

    public String[] getFacets() {
        return facets;
    }

    public String[] getContentTypes() {
        return contentTypes;
    }

    public List<ContentSearchFilter> getFacetFilters() {
        return facetFilters;
    }

    public List<ContentSearchFilter> getImpliedFilters() {
        return impliedFilters;
    }

    public List<ContentSortCriteria> getSortCriteria() {
        return sortCriteria;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public long getLimit() {
        return limit;
    }

    public String getMode() {
        return mode;
    }

    public static class SearchBuilder {

        private String query;
        private long limit;
        private String mode;
        private int age;
        private int grade;
        private String medium;
        private String board;
        private String[] createdBy;
        private String[] audience;
        private String[] channel;
        private String[] contentStatusArray;
        private String[] facets;
        private String[] contentTypes;
        private List<ContentSortCriteria> sortCriteria;

        public SearchBuilder() {
            this.query = "";
            this.limit = DEFAULT_LIMIT;
        }

        /**
         * Query string.
         */
        public SearchBuilder query(String query) {
            this.query = query;
            return this;
        }

        /**
         * Search results limit.
         */
        public SearchBuilder limit(long limit) {
            this.limit = limit;
            return this;
        }

        /**
         * List of sort criteria {@link ContentSortCriteria}.
         */
        public SearchBuilder sort(List<ContentSortCriteria> sortCriteria) {
            this.sortCriteria = sortCriteria;
            return this;
        }

        public SearchBuilder softFilters() {
            this.mode = "soft";
            return this;
        }

        /**
         * Age of the user.
         */
        public SearchBuilder age(int age) {
            this.age = age;
            return this;
        }

        /**
         * Grade of the user.
         */
        public SearchBuilder grade(int standard) {
            this.grade = standard;
            return this;
        }

        /**
         * Medium of the user.
         */
        public SearchBuilder medium(String medium) {
            this.medium = medium;
            return this;
        }

        /**
         * Board of the user.
         */
        public SearchBuilder board(String board) {
            this.board = board;
            return this;
        }

        /**
         * Created by.
         */
        public SearchBuilder createdBy(String[] createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * Array of audience. i.e. "Learner", "Instructor".
         */
        public SearchBuilder audience(String[] audience) {
            this.audience = audience;
            return this;
        }

        public SearchBuilder channel(String[] channel) {
            this.channel = channel;
            return this;
        }

        /**
         * Array of status. i.e. "Live", "Draft"
         */
        public SearchBuilder contentStatusArray(String[] contentStatusArray) {
            this.contentStatusArray = contentStatusArray;
            return this;
        }

        /**
         * Array of facets. i.e. "contentType", "domain", "ageGroup", "language", "gradeLevel"
         */
        public SearchBuilder facets(String[] facets) {
            this.facets = facets;
            return this;
        }

        /**
         * Array of contentTypes. i.e. "Story", "Worksheet", "Game", "Collection", "TextBook", "Resource", "Course", "LessonPlan".
         */
        public SearchBuilder contentTypes(String[] contentTypes) {
            this.contentTypes = contentTypes;
            return this;
        }

        public ContentSearchCriteria build() {
            if (contentStatusArray == null || contentStatusArray.length == 0) {
                this.contentStatusArray = new String[]{"Live"};
            }

            if (facets == null || facets.length == 0) {
                this.facets = new String[]{"contentType", "domain", "ageGroup", "language", "gradeLevel"};
            }

            if (contentTypes == null || contentTypes.length == 0) {
                this.contentTypes = new String[]{"Story", "Worksheet", "Game", "Collection", "TextBook", "Resource"};
            }

            return new ContentSearchCriteria(query, limit, mode, age, grade, medium, board, createdBy,
                    audience, channel, contentStatusArray, facets, contentTypes, sortCriteria, SearchType.SEARCH);
        }
    }


    public static class FilterBuilder {

        private String query;
        private long limit;
        private String mode;
        private String[] facets;
        private String[] contentTypes;
        private List<ContentSearchFilter> facetFilters;
        private List<ContentSearchFilter> impliedFilters;
        private List<ContentSortCriteria> sortCriteria;

        public FilterBuilder() {
            this.query = "";
            this.limit = DEFAULT_LIMIT;
        }

        public FilterBuilder query(String query) {
            this.query = query;
            return this;
        }

        public FilterBuilder limit(long limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Array of facets. i.e. "contentType", "domain", "ageGroup", "language", "gradeLevel"
         */
        public FilterBuilder facets(String[] facets) {
            this.facets = facets;
            return this;
        }

        /**
         * Array of contentTypes. i.e. "Story", "Worksheet", "Game", "Collection", "TextBook", "Resource", "Course", "LessonPlan".
         */
        public FilterBuilder contentTypes(String[] contentTypes) {
            this.contentTypes = contentTypes;
            return this;
        }

        public FilterBuilder impliedFilters(List<ContentSearchFilter> impliedFilters) {
            this.impliedFilters = impliedFilters;
            return this;
        }

        public FilterBuilder facetFilters(List<ContentSearchFilter> facetFilters) {
            this.facetFilters = facetFilters;
            return this;
        }

        public FilterBuilder sort(List<ContentSortCriteria> sortCriteria) {
            this.sortCriteria = sortCriteria;
            return this;
        }

        public FilterBuilder softFilters() {
            this.mode = "soft";
            return this;
        }

        public ContentSearchCriteria build() {
            if (facets == null || facets.length == 0) {
                this.facets = new String[]{"contentType", "domain", "ageGroup", "language", "gradeLevel"};
            }

            if (contentTypes == null || contentTypes.length == 0) {
                this.contentTypes = new String[]{"Story", "Worksheet", "Game", "Collection", "TextBook", "Resource"};
            }

            return new ContentSearchCriteria(query, limit, mode, facets, contentTypes, facetFilters, impliedFilters, sortCriteria, SearchType.FILTER);
        }
    }
}
