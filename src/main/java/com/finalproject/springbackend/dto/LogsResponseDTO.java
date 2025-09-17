package com.finalproject.springbackend.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LogsResponseDTO {

    private List<ContentLogs> content;
    private PageableLogs pageable;
    private Integer totalElements;
    private Integer totalPages;
    private boolean first;
    private boolean last;
    private Integer numberOfElements;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ContentLogs{
        private String id;
        private String time;
        private DataLogs data;
        private String processingTime;
    }
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DataLogs{
        private String methodName;
        private String resourceName;
        private AuthenticationInfoLogs authenticationInfo;
        private AuthorizationInfoLogs authorizationInfo;
        private List<ClientAddressLogs> clientAddress;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthenticationInfoLogs{
        private String principal;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthorizationInfoLogs{
        private boolean granted;
        private String resourceType;
        private String resourceName;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClientAddressLogs {
        private String ip;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PageableLogs {
        private Integer page;
        private Integer size;
        private SortLogs sort;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SortLogs {
        private boolean sorted;
        private String by;
        private String direction;
    }

}
