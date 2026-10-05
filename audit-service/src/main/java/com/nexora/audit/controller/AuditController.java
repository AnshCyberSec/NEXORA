package com.nexora.audit.controller;

import com.nexora.audit.common.ApiResponse;
import com.nexora.audit.dto.request.CreateAuditRequest;
import com.nexora.audit.dto.response.AuditResponse;
import com.nexora.audit.enums.AuditAction;
import com.nexora.audit.enums.AuditSource;
import com.nexora.audit.enums.AuditStatus;
import com.nexora.audit.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    private static final String LOG_PREFIX = "[AUDIT-CONTROLLER]";

    @PostMapping
    public ResponseEntity<ApiResponse<AuditResponse>> createAuditLog(
            @Valid @RequestBody CreateAuditRequest request) {
        log.info("{} Create audit log for userId: {}, action: {}, source: {}",
                LOG_PREFIX, request.getUserId(), request.getAction(), request.getSource());

        AuditResponse response = auditService.createAuditLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Audit log created successfully"));
    }

    @GetMapping("/{auditLogId}")
    public ResponseEntity<ApiResponse<AuditResponse>> getAuditLogById(
            @PathVariable String auditLogId) {
        log.debug("{} Get audit log by id: {}", LOG_PREFIX, auditLogId);

        AuditResponse response = auditService.getAuditLogById(auditLogId);
        return ResponseEntity.ok(ApiResponse.success(response, "Audit log fetched successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getAuditLogsByUserId(
            @PathVariable String userId) {
        log.debug("{} Get audit logs for userId: {}", LOG_PREFIX, userId);

        List<AuditResponse> responses = auditService.getAuditLogsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Audit logs fetched successfully"));
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getAuditLogsByReferenceId(
            @PathVariable String referenceId) {
        log.debug("{} Get audit logs for referenceId: {}", LOG_PREFIX, referenceId);

        List<AuditResponse> responses = auditService.getAuditLogsByReferenceId(referenceId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Audit logs fetched successfully"));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getAuditLogsByAction(
            @PathVariable AuditAction action) {
        log.debug("{} Get audit logs for action: {}", LOG_PREFIX, action);

        List<AuditResponse> responses = auditService.getAuditLogsByAction(action);
        return ResponseEntity.ok(ApiResponse.success(responses, "Audit logs fetched successfully"));
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getAuditLogsBySource(
            @PathVariable AuditSource source) {
        log.debug("{} Get audit logs for source: {}", LOG_PREFIX, source);

        List<AuditResponse> responses = auditService.getAuditLogsBySource(source);
        return ResponseEntity.ok(ApiResponse.success(responses, "Audit logs fetched successfully"));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getAuditLogsByStatus(
            @PathVariable AuditStatus status) {
        log.debug("{} Get audit logs for status: {}", LOG_PREFIX, status);

        List<AuditResponse> responses = auditService.getAuditLogsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Audit logs fetched successfully"));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("{} Get audit logs between {} and {}", LOG_PREFIX, startDate, endDate);

        List<AuditResponse> responses = auditService.getAuditLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Audit logs fetched successfully"));
    }

    @GetMapping("/user/{userId}/action/{action}")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getAuditLogsByUserIdAndAction(
            @PathVariable String userId,
            @PathVariable AuditAction action) {
        log.debug("{} Get audit logs for userId: {}, action: {}", LOG_PREFIX, userId, action);

        List<AuditResponse> responses = auditService.getAuditLogsByUserIdAndAction(userId, action);
        return ResponseEntity.ok(ApiResponse.success(responses, "Audit logs fetched successfully"));
    }
}