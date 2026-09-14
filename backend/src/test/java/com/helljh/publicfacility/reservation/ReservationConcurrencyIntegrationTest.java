package com.helljh.publicfacility.reservation;

import com.helljh.publicfacility.common.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationConcurrencyIntegrationTest extends IntegrationTestSupport {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {

        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM room");
        jdbcTemplate.update("DELETE FROM facility");
        jdbcTemplate.update("DELETE FROM users");

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update("""
                INSERT INTO users (
                    id,
                    email,
                    password,
                    name,
                    role,
                    status,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L,
                "concurrency@test.com",
                "password",
                "동시성테스트",
                "USER",
                "ACTIVE",
                now,
                now
        );

        jdbcTemplate.update("""
                INSERT INTO facility (
                    id,
                    name,
                    address,
                    status,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                1L,
                "테스트 시설",
                "서울시 테스트구",
                "ACTIVE",
                now,
                now
        );

        jdbcTemplate.update("""
                INSERT INTO room (
                    id,
                    facility_id,
                    name,
                    capacity,
                    is_active,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                1L,
                1L,
                "동시성 테스트 회의실",
                10,
                true,
                now,
                now
        );
    }

    @Test
    void 동일_시간대_동시예약은_한_건만_성공한다() throws Exception {

        int requestCount = 10;

        ExecutorService executorService =
                Executors.newFixedThreadPool(requestCount);

        CountDownLatch readyLatch =
                new CountDownLatch(requestCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<Result>> futures =
                new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {

            int index = i;

            futures.add(
                    executorService.submit(() -> {

                        readyLatch.countDown();
                        startLatch.await();

                        try {

                            jdbcTemplate.update("""
                                    INSERT INTO reservation (
                                        reservation_number,
                                        user_id,
                                        room_id,
                                        reservation_date,
                                        start_time,
                                        end_time,
                                        people_count,
                                        status,
                                        created_at,
                                        updated_at
                                    )
                                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                    """,
                                    "RSV-CONCURRENCY-" + index,
                                    1L,
                                    1L,
                                    LocalDate.of(2026, 9, 20),
                                    LocalTime.of(10, 0),
                                    LocalTime.of(11, 0),
                                    1,
                                    "RESERVED",
                                    LocalDateTime.now(),
                                    LocalDateTime.now()
                            );

                            return Result.SUCCESS;

                        } catch (DataIntegrityViolationException e) {

                            return Result.CONFLICT;

                        } catch (Exception e) {

                            return Result.ERROR;
                        }
                    })
            );
        }

        // 10개 스레드가 모두 준비될 때까지 대기
        readyLatch.await();

        // 동시에 시작
        startLatch.countDown();

        int successCount = 0;
        int conflictCount = 0;
        int errorCount = 0;

        for (Future<Result> future : futures) {

            Result result = future.get();

            switch (result) {
                case SUCCESS -> successCount++;
                case CONFLICT -> conflictCount++;
                case ERROR -> errorCount++;
            }
        }

        executorService.shutdown();

        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(9);
        assertThat(errorCount).isZero();

        Integer reservationCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM reservation
                        WHERE room_id = ?
                          AND reservation_date = ?
                          AND status = 'RESERVED'
                        """,
                        Integer.class,
                        1L,
                        LocalDate.of(2026, 9, 20)
                );

        assertThat(reservationCount).isEqualTo(1);
    }

    enum Result {
        SUCCESS,
        CONFLICT,
        ERROR
    }
}