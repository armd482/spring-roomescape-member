package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.InvalidRequestValueException;

class ReservationTest {
    private ReservationTime testTime;
    private Theme testTheme;

    @BeforeEach
    void setUp() {
        testTime = new ReservationTime(1L, LocalTime.of(10, 0));
        testTheme = new Theme(1L, "이름", "설명", "https://image.com");
    }

    @Test
    @DisplayName("정상 생성 테스트")
    void initTest() {
        long id = 1L;
        String name = "브라운";
        LocalDate date = LocalDate.now();

        Reservation info = new Reservation(id, name, date, testTime, testTheme);

        assertAll(
                () -> assertThat(info.id()).isEqualTo(id),
                () -> assertThat(info.name()).isEqualTo(name),
                () -> assertThat(info.date()).isEqualTo(date),
                () -> assertThat(info.time()).isEqualTo(testTime),
                () -> assertThat(info.theme()).isEqualTo(testTheme)
        );
    }

    @Test
    @DisplayName("String 형식의 날짜 정상 생성 테스트")
    void initByStringDateTest() {
        String dateStr = "2026-05-13";
        Reservation info = new Reservation(1L, "브라운", dateStr, testTime, testTheme);

        assertThat(info.date()).isEqualTo(LocalDate.parse(dateStr));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("이름이 비어있거나 공백이면 예외가 발생한다")
    void invalidNameTest(String invalidName) {
        assertThatThrownBy(() -> new Reservation(1L, invalidName, LocalDate.now(), testTime, testTheme))
                .isInstanceOf(InvalidRequestValueException.class)
                .hasMessage("이름은 필수입니다.");
    }

    @Test
    @DisplayName("날짜가 null이면 예외가 발생한다")
    void nullDateTest() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", (LocalDate) null, testTime, testTheme))
                .isInstanceOf(InvalidRequestValueException.class)
                .hasMessage("날짜는 필수입니다.");
    }

    @Test
    @DisplayName("잘못된 형식의 날짜 문자열은 예외가 발생한다")
    void invalidStringDateTest() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", "2026/05/13", testTime, testTheme))
                .isInstanceOf(InvalidRequestValueException.class)
                .hasMessage("유효하지 않은 날짜입니다.");
    }

    @Test
    @DisplayName("ReservationTime이 null이면 예외가 발생한다")
    void nullTimeTest() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", LocalDate.now(), null, testTheme))
                .isInstanceOf(InvalidRequestValueException.class)
                .hasMessage("시간 정보는 필수입니다.");
    }

    @Test
    @DisplayName("Theme이 null이면 예외가 발생한다")
    void nullThemeTest() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", LocalDate.now(), testTime, null))
                .isInstanceOf(InvalidRequestValueException.class)
                .hasMessage("테마 정보는 필수입니다.");
    }

    @Test
    @DisplayName("수정 값이 기존 정보와 하나라도 다른 경우 검증 성공 테스트")
    void validateEqualValueSuccess() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, testTime, testTheme);

        assertThatCode(() -> reservation.validateEqualValue("테스트", futureDate, testTime.id(), testTheme.id()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수정하려는 정보가 기존 정보와 완전히 동일한 경우 예외 테스트")
    void validateEqualValueSameFail() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, testTime, testTheme);

        assertThatThrownBy(() -> reservation.validateEqualValue("브라운", futureDate, testTime.id(), testTheme.id()))
                .isInstanceOf(InvalidRequestValueException.class)
                .hasMessage("기존 정보와 동일하여 수정할 내용이 없습니다.");
    }

    @Test
    @DisplayName("현재 시점 기준 미래 예약 날짜에 대한 과거 여부 검증 성공 테스트")
    void validDateReservationPastDateTimeSuccess() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation futureReservation = new Reservation(1L, "브라운", futureDate, testTime, testTheme);

        assertThatCode(() -> futureReservation.validDateReservationPastDateTime("이미 지난 예약은 수정할 수 없습니다."))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 지난 시점의 예약 날짜일 경우 과거 여부 검증 예외 테스트")
    void validDateReservationPastDateTimeFail() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = new Reservation(1L, "브라운", pastDate, testTime, testTheme);

        String errorMessage = "이미 지난 예약은 수정할 수 없습니다.";

        assertThatThrownBy(() -> pastReservation.validDateReservationPastDateTime(errorMessage))
                .isInstanceOf(InvalidRequestValueException.class)
                .hasMessage(errorMessage);
    }
}