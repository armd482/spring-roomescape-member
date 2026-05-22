package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import roomescape.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ForbiddenException;
import roomescape.exception.InvalidRequestValueException;
import roomescape.exception.UnauthorizedException;

public record Reservation(long id, String name, LocalDate date, ReservationTime time, Theme theme) {
    private static final String INVALID_NAME_NULL = "이름은 필수입니다.";
    private static final String INVALID_DATE_NULL = "날짜는 필수입니다.";
    private static final String INVALID_DATE_FORMAT = "유효하지 않은 날짜입니다.";
    private static final String INVALID_TIME_NULL = "시간 정보는 필수입니다.";
    private static final String INVALID_THEME_NULL = "테마 정보는 필수입니다.";
    private static final String CANNOT_UPDATE_SAME_VALUE = "기존 정보와 동일하여 수정할 내용이 없습니다.";


    public Reservation {
        validateName(name);
        validateDateValue(date);
        validateObjects(time, theme);
    }

    public Reservation(long id, String name, String date, ReservationTime time, Theme theme) {
        this(id, name, parseDate(date), time, theme);
    }

    public void validateEqualValue(String name, LocalDate date, long timeId, long themeId) {
        if(isEqualValue(name, date, timeId, themeId)) {
            throw new InvalidRequestValueException(CANNOT_UPDATE_SAME_VALUE);
        }
    }

    public void validateEditablePermission(String name, String errorMessage) {
        if(name == null) {
            throw new UnauthorizedException(errorMessage);
        }

        if (!name.equals(this.name)) {
            throw new ForbiddenException(errorMessage);
        }
    }

    public void validDateReservationPastDateTime(String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationTime = LocalDateTime.of(date, time.startAt());

        if(reservationTime.isBefore(now)) {
            throw new InvalidRequestValueException(errorMessage);
        }
    }

    private boolean isEqualValue(String name, LocalDate date, long timeId, long themeId) {
        return (
                this.name.equals(name) && this.date.equals(date) && this.time().id() == timeId && this.theme.id() == themeId
        );
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestValueException(INVALID_NAME_NULL);
        }
    }

    private static LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            throw new InvalidRequestValueException(INVALID_DATE_NULL);
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestValueException(INVALID_DATE_FORMAT);
        }
    }

    private static void validateDateValue(LocalDate date) {
        if (date == null) {
            throw new InvalidRequestValueException(INVALID_DATE_NULL);
        }
    }

    private static void validateObjects(ReservationTime time, Theme theme) {
        if (time == null) {
            throw new InvalidRequestValueException(INVALID_TIME_NULL);
        }
        if (theme == null) {
            throw new InvalidRequestValueException(INVALID_THEME_NULL);
        }
    }
}
