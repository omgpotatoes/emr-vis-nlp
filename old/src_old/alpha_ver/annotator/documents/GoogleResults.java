package annotator.documents;

import java.util.List;

/**
 *
 * Represents the result returned from a query to the google api. Adapted from
 * example provided at:
 * http://stackoverflow.com/questions/3727662/how-can-you-search-google-programmatically-java-api
 *
 * @author BalusC, alexander.p.conrad@gmail.com
 */
public class GoogleResults {

    private ResponseData responseData;

    public ResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(ResponseData responseData) {
        this.responseData = responseData;
    }

    public String toString() {
        return "ResponseData[" + responseData + "]";
    }

    static class ResponseData {

        private List<Result> results;

        public List<Result> getResults() {
            return results;
        }

        public void setResults(List<Result> results) {
            this.results = results;
        }

        public String toString() {
            return "Results[" + results + "]";
        }
    }

    static class Result {

        private String title;
        // web-search-specific fields
        private String url;
        // blog-search-specific fields
        private String blogUrl;
        private String postUrl;
        private String author;
        private String publishedDate;
        private String titleNoFormatting;
        private String content;
        private String html;

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }

        public String getUrl() {
            return url;
        }

        public String getBlogUrl() {
            return blogUrl;
        }

        public String getPostUrl() {
            return postUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setBlogUrl(String blogUrl) {
            this.blogUrl = blogUrl;
        }

        public void setPostUrl(String postUrl) {
            this.postUrl = postUrl;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String toString() {
            return "Result[url:" + url + ", url:" + url + ",title:" + title + "]";
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public String getTitleNoFormatting() {
            return titleNoFormatting;
        }

        public void setTitleNoFormatting(String titleNoFormatting) {
            this.titleNoFormatting = titleNoFormatting;
        }
    }
}
