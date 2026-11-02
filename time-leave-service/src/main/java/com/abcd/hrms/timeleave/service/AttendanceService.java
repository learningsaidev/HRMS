package com.abcd.hrms.timeleave.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.common.messaging.KafkaTopicConstants;
import com.abcd.hrms.common.messaging.event.CanonicalEvent;
import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.timeleave.dto.AttendanceLockRequest;
import com.abcd.hrms.timeleave.dto.DailyAttendanceDto;
import com.abcd.hrms.timeleave.dto.PayrollAttendanceSummaryDto;
import com.abcd.hrms.timeleave.dto.PunchRequest;
import com.abcd.hrms.timeleave.entity.AttendanceLock;
import com.abcd.hrms.timeleave.entity.AttendancePunch;
import com.abcd.hrms.timeleave.entity.DailyAttendance;
import com.abcd.hrms.timeleave.repository.AttendanceLockRepository;
import com.abcd.hrms.timeleave.repository.AttendancePunchRepository;
import com.abcd.hrms.timeleave.repository.DailyAttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendancePunchRepository punchRepository;
    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final AttendanceLockRepository attendanceLockRepository;
    private final OutboxPublisherService outboxPublisherService;

    @Transactional
    public DailyAttendanceDto recordPunch(PunchRequest request) {
        UUID companyId = TenantContext.getCompanyId() != null ? TenantContext.getCompanyId() : UUID.fromString("d0000000-0000-0000-0000-000000000001");
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        // Check if attendance is locked for this period
        if (attendanceLockRepository.existsByCompanyIdAndPeriodYearAndPeriodMonthAndIsLockedTrue(companyId, today.getYear(), today.getMonthValue())) {
            throw new BusinessValidationException("Attendance edits are locked for this period", "ERR_ATTENDANCE_LOCKED");
        }

        AttendancePunch punch = AttendancePunch.builder()
                .companyId(companyId)
                .employeeId(request.getEmployeeId())
                .punchTime(now)
                .punchType(request.getPunchType().toUpperCase())
                .deviceId(request.getDeviceId())
                .location(request.getLocation())
                .build();
        punchRepository.save(punch);

        DailyAttendance daily = dailyAttendanceRepository.findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), today)
                .orElseGet(() -> DailyAttendance.builder()
                        .companyId(companyId)
                        .employeeId(request.getEmployeeId())
                        .attendanceDate(today)
                        .status("PRESENT")
                        .build());

        if ("IN".equalsIgnoreCase(request.getPunchType())) {
            if (daily.getFirstPunchIn() == null) {
                daily.setFirstPunchIn(now);
            }
        } else if ("OUT".equalsIgnoreCase(request.getPunchType())) {
            daily.setLastPunchOut(now);
            if (daily.getFirstPunchIn() != null) {
                long durationSeconds = Duration.between(daily.getFirstPunchIn(), now).getSeconds();
                BigDecimal hours = BigDecimal.valueOf(durationSeconds / 3600.0);
                daily.setTotalWorkedHours(hours);
                if (hours.compareTo(BigDecimal.valueOf(8.0)) > 0) {
                    daily.setOvertimeHours(hours.subtract(BigDecimal.valueOf(8.0)));
                }
            }
        }

        DailyAttendance saved = dailyAttendanceRepository.save(daily);
        return DailyAttendanceDto.builder()
                .id(saved.getId())
                .employeeId(saved.getEmployeeId())
                .attendanceDate(saved.getAttendanceDate())
                .firstPunchIn(saved.getFirstPunchIn())
                .lastPunchOut(saved.getLastPunchOut())
                .totalWorkedHours(saved.getTotalWorkedHours())
                .overtimeHours(saved.getOvertimeHours())
                .status(saved.getStatus())
                .isRegularized(saved.isRegularized())
                .build();
    }

    @Transactional
    public AttendanceLock lockAttendancePeriod(AttendanceLockRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        if (attendanceLockRepository.existsByCompanyIdAndPeriodYearAndPeriodMonthAndIsLockedTrue(companyId, request.getYear(), request.getMonth())) {
            throw new BusinessValidationException("Attendance is already locked for period: " + request.getYear() + "-" + request.getMonth(), "ERR_ALREADY_LOCKED");
        }

        String lockedBy = TenantContext.getUsername() != null ? TenantContext.getUsername() : "PAYROLL_ADMIN";
        AttendanceLock lock = AttendanceLock.builder()
                .companyId(companyId)
                .periodYear(request.getYear())
                .periodMonth(request.getMonth())
                .isLocked(true)
                .lockedBy(lockedBy)
                .lockedAt(Instant.now())
                .build();

        AttendanceLock savedLock = attendanceLockRepository.save(lock);

        // Publish AttendancePeriodLocked.v1 to Kafka via Transactional Outbox
        Map<String, Object> payload = new HashMap<>();
        payload.put("companyId", companyId);
        payload.put("year", request.getYear());
        payload.put("month", request.getMonth());
        payload.put("lockedBy", lockedBy);
        payload.put("lockedAt", savedLock.getLockedAt().toString());

        CanonicalEvent<Map<String, Object>> event = CanonicalEvent.of(
                "AttendancePeriodLocked.v1",
                "ATTENDANCE_PERIOD",
                request.getYear() + "-" + request.getMonth(),
                companyId,
                null,
                "time-leave-service",
                TenantContext.getCorrelationId(),
                payload
        );

        outboxPublisherService.enqueueEvent(
                KafkaTopicConstants.TOPIC_TIME_LEAVE_EVENTS,
                "ATTENDANCE_PERIOD",
                request.getYear() + "-" + request.getMonth(),
                event
        );

        return savedLock;
    }

    @Transactional(readOnly = true)
    public PayrollAttendanceSummaryDto getPayrollAttendanceSummary(UUID employeeId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int totalDays = ym.lengthOfMonth();

        List<DailyAttendance> attendances = dailyAttendanceRepository.findByEmployeeIdAndAttendanceDateBetween(employeeId, start, end);

        BigDecimal presentDays = BigDecimal.ZERO;
        BigDecimal lopDays = BigDecimal.ZERO;
        BigDecimal paidLeaveDays = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;

        for (DailyAttendance da : attendances) {
            String status = da.getStatus();
            if ("PRESENT".equalsIgnoreCase(status)) {
                presentDays = presentDays.add(BigDecimal.ONE);
            } else if ("HALF_DAY".equalsIgnoreCase(status)) {
                presentDays = presentDays.add(BigDecimal.valueOf(0.5));
                lopDays = lopDays.add(BigDecimal.valueOf(0.5));
            } else if ("ON_LEAVE".equalsIgnoreCase(status)) {
                paidLeaveDays = paidLeaveDays.add(BigDecimal.ONE);
            } else if ("ABSENT".equalsIgnoreCase(status)) {
                lopDays = lopDays.add(BigDecimal.ONE);
            }
            if (da.getOvertimeHours() != null) {
                overtimeHours = overtimeHours.add(da.getOvertimeHours());
            }
        }

        // Default: If no attendance records present in local test, assume full payable days (e.g. 30 days)
        BigDecimal payableDays = BigDecimal.valueOf(totalDays).subtract(lopDays);

        return PayrollAttendanceSummaryDto.builder()
                .employeeId(employeeId)
                .periodYear(year)
                .periodMonth(month)
                .totalCalendarDays(BigDecimal.valueOf(totalDays))
                .payableDays(payableDays)
                .lossOfPayDays(lopDays)
                .paidLeaveDays(paidLeaveDays)
                .overtimeHours(overtimeHours)
                .build();
    }
}
