package com.example.slagalica.domain.service.ranking;

import com.example.slagalica.domain.model.ranking.RankingCycle;
import com.example.slagalica.domain.model.ranking.RankingCycleType;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public final class RankingCycleUtils {

    private static final ZoneId ZONE = ZoneId.of("Europe/Belgrade");
    private static final DateTimeFormatter ID_DATE =
            DateTimeFormatter.ofPattern("yyyy_MM_dd");
    private static final DateTimeFormatter ID_MONTH =
            DateTimeFormatter.ofPattern("yyyy_MM");
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    private RankingCycleUtils() {
    }

    public static RankingCycle currentCycle(RankingCycleType type, long nowMillis) {
        LocalDate today = Instant.ofEpochMilli(nowMillis)
                .atZone(ZONE)
                .toLocalDate();

        if (type == RankingCycleType.WEEKLY) {
            LocalDate startDate = today.with(
                    TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
            );
            LocalDate endDateExclusive = startDate.plusWeeks(1);

            return new RankingCycle(
                    "WEEKLY_" + ID_DATE.format(startDate),
                    RankingCycleType.WEEKLY,
                    toMillis(startDate),
                    toMillis(endDateExclusive),
                    false,
                    0L
            );
        }

        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDateExclusive = startDate.plusMonths(1);

        return new RankingCycle(
                "MONTHLY_" + ID_MONTH.format(startDate),
                RankingCycleType.MONTHLY,
                toMillis(startDate),
                toMillis(endDateExclusive),
                false,
                0L
        );
    }

    public static String formatRange(RankingCycle cycle) {
        LocalDate start = Instant.ofEpochMilli(cycle.getStartMillis())
                .atZone(ZONE)
                .toLocalDate();

        LocalDate inclusiveEnd = Instant.ofEpochMilli(cycle.getEndMillis())
                .atZone(ZONE)
                .toLocalDate()
                .minusDays(1);

        return DISPLAY_DATE.format(start) + " – " + DISPLAY_DATE.format(inclusiveEnd);
    }

    private static long toMillis(LocalDate date) {
        return date.atStartOfDay(ZONE).toInstant().toEpochMilli();
    }
}
