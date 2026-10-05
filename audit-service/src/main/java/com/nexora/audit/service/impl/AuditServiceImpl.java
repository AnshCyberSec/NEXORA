package com.nexora.audit.service.impl;

import com.nexora.audit.dto.request.CreateAuditRequest;
import com.nexora.audit.dto.response.AuditResponse;
import com.nexora.audit.entity.AuditLog;
import com.nexora.audit.enums.AuditAction;
import com.nexora.audit.enums.AuditSource;
import com.nexora.audit.enums.AuditStatus;
import com.nexora.audit.exception.AuditLogNotFoundException;
import com.nexora.audit.mapper.AuditLogMapper;
import com.nexora.audit.repository.AuditLogRepository;
import com.nexora.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    private static final String LOG_PREFIX = "[AUDIT-SERVICE]";

    @Override
    public AuditResponse createAuditLog(CreateAuditRequest request) {
        log.info("{} Creating audit log for userId: {}, action: {}, source: {}, status: {}",
                LOG_PREFIX, request.getUserId(), request.getAction(), request.getSource(), request.getStatus());

        AuditLog auditLog = auditLogMapper.toEntity(request);
        AuditLog savedAuditLog = auditLogRepository.save(auditLog);

        log.info("{} Audit log created successfully with id: {}, referenceId: {}",
                LOG_PREFIX, savedAuditLog.getId(), savedAuditLog.getReferenceId());

        return auditLogMapper.toResponse(savedAuditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditResponse getAuditLogById(String auditLogId) {
        log.debug("{} Fetching audit log by id: {}", LOG_PREFIX, auditLogId);

        AuditLog auditLog = auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> {
                    log.warn("{} Audit log not found with id: {}", LOG_PREFIX, auditLogId);
                    return new AuditLogNotFoundException(auditLogId);
                });

        return auditLogMapper.toResponse(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditLogsByUserId(String userId) {
        log.debug("{} Fetching audit logs for userId: {}", LOG_PREFIX, userId);

        List<AuditLog> auditLogs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return auditLogs.stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditLogsByReferenceId(String referenceId) {
        log.debug("{} Fetching audit logs for referenceId: {}", LOG_PREFIX, referenceId);

        List<AuditLog> auditLogs = auditLogRepository.findByReferenceId(referenceId);

        return auditLogs.stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditLogsByAction(AuditAction action) {
        log.debug("{} Fetching audit logs for action: {}", LOG_PREFIX, action);

        List<AuditLog> auditLogs = auditLogRepository.findByAction(action);

        return auditLogs.stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditLogsBySource(AuditSource source) {
        log.debug("{} Fetching audit logs for source: {}", LOG_PREFIX, source);

        List<AuditLog> auditLogs = auditLogRepository.findBySource(source);

        return auditLogs.stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditLogsByStatus(AuditStatus status) {
        log.debug("{} Fetching audit logs for status: {}", LOG_PREFIX, status);

        List<AuditLog> auditLogs = auditLogRepository.findByStatus(status);

        return auditLogs.stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("{} Fetching audit logs between {} and {}", LOG_PREFIX, startDate, endDate);

        List<AuditLog> auditLogs = auditLogRepository.findByCreatedAtBetween(startDate, endDate);

        return auditLogs.stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditLogsByUserIdAndAction(String userId, AuditAction action) {
        log.debug("{} Fetching audit logs for userId: {}, action: {}", LOG_PREFIX, userId, action);

        List<AuditLog> auditLogs = auditLogRepository.findByUserIdAndAction(userId, action);

        return auditLogs.stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }
}