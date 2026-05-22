package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationCommand;
import roomescape.domain.reservationTime.ReservationTime;
import roomescape.exception.ConflictException;
import roomescape.exception.NotFoundResourceException;
import roomescape.repository.theme.ThemeRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationTime.ReservationTimeRepository;

@Service
public class ReservationService {
    private static final String DUPLICATED_RESERVATION_REQUEST = "해당 날짜, 시간, 테마의 예약이 존재하여 예약할 수 없습니다.";
    private static final String INVALID_TIME_ID = "존재하지 않은 시간 id입니다.";
    private static final String INVALID_THEME_ID = "존재하지 않은 테마 id입니다.";
    private static final String INVALID_RESERVATION_TIME_ID = "존재하지 않은 시간 id입니다.";
    private static final String INVALID_RESERVATION_ID = "존재하지 않는 예약 id입니다.";
    private static final String UNAUTHORIZED_UPDATE_RESERVATION_REQUEST = "해당 예약을 수정할 권한이 없습니다.";
    private static final String UNAUTHORIZED_DELETE_RESERVATION_REQUEST = "해당 예약을 삭제할 권한이 없습니다.";
    private static final String CANNOT_UPDATE_RESERVATION = "수정하려는 예약이 존재하지 않아서 수정할 수 없습니다.";
    private static final String CANNOT_UPDATE_PAST_RESERVATION = "이미 지난 예약은 수정할 수 없습니다.";
    private static final String CANNOT_DELETE_PAST_RESERVATION = "이미 지난 예약은 삭제할 수 없습니다.";

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<Reservation> getAllReservation(String name) {
        return reservationRepository.getAllReservation(name);
    }

    public Reservation addReservation(ReservationCommand reservationCommand) {
        validateAddReservation(reservationCommand);

        long id = reservationRepository.addReservation(reservationCommand);
        return getReservationWithTimeAndData(id);
    }

    public void deleteReservation(long id, String name) {
        validateDeleteReservation(id, name);
        reservationRepository.deleteReservation(id);
    }

    public void updateReservation(long id, String name, ReservationCommand reservationCommand) {
        Reservation reservation = getReservationWithTimeAndData(id);

        validateUpdateReservation(name, reservationCommand, reservation);

        int updatedRow = reservationRepository.updateAll(id, reservationCommand);

        if (updatedRow == 0) {
            throw new NotFoundResourceException(CANNOT_UPDATE_RESERVATION);
        }
    }

    private Reservation getReservationWithTimeAndData(long id) {
        return getData(() -> reservationRepository.getReservationWithTimeAndTheme(id), INVALID_RESERVATION_ID);
    }

    private ReservationTime getReservationTime(long id) {
        return getData(() -> reservationTimeRepository.getReservationTime(id), INVALID_RESERVATION_TIME_ID);
    }

    private <T> T getData(Supplier<Optional<T>> supplier, String errorMessage) {
        Optional<T> optionalData = supplier.get();

        if(optionalData.isEmpty()) {
            throw new NotFoundResourceException(errorMessage);
        }

        return optionalData.get();
    }

    private void validateAddReservation(ReservationCommand reservationCommand) {
        validateAvailableReservationValue(reservationCommand.timeId(), reservationCommand.themeId(), reservationCommand.date());

        ReservationTime reservationTime = getReservationTime(reservationCommand.timeId());
        reservationCommand.validatePastDateTime(reservationTime);
    }

    private void validateDeleteReservation(long id, String name) {
        Reservation reservation = getReservationWithTimeAndData(id);

        reservation.validateEditablePermission(name, UNAUTHORIZED_DELETE_RESERVATION_REQUEST);

        reservation.validDateReservationPastDateTime(CANNOT_DELETE_PAST_RESERVATION);
    }

    private void validateUpdateReservation(String name, ReservationCommand reservationCommand, Reservation reservation) {
        reservation.validateEditablePermission(name, UNAUTHORIZED_UPDATE_RESERVATION_REQUEST);

        validateAvailableReservationValue(reservationCommand.timeId(), reservationCommand.themeId(), reservationCommand.date());

        ReservationTime reservationTime = getReservationTime(reservationCommand.timeId());
        reservationCommand.validatePastDateTime(reservationTime);

        reservation.validDateReservationPastDateTime(CANNOT_UPDATE_PAST_RESERVATION);
        reservation.validateEqualValue(reservationCommand.name(), reservationCommand.date(), reservationCommand.timeId(), reservationCommand.themeId());

    }

    private void validateAvailableReservationValue(long timeId, long themeId, LocalDate date) {
        if(!reservationTimeRepository.isExistsById(timeId)) {
            throw new NotFoundResourceException(INVALID_TIME_ID);
        }
        if(!themeRepository.isExistsById(themeId)) {
            throw new NotFoundResourceException(INVALID_THEME_ID);
        }

        if (reservationRepository.existsByTimeIdAndThemeIdAndDate(timeId, themeId, date)) {
            throw new ConflictException(DUPLICATED_RESERVATION_REQUEST);
        }
    }
}
