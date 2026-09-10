package com.helljh.publicfacility.audit.repository;

import com.helljh.publicfacility.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {
}