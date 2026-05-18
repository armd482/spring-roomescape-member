package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationCommand;

public interface ReservationRepository {
    Optional<Reservation> getReservationWithTimeAndTheme(long id);
    List<Reservation> getAllReservation(String name);
    long addReservation(ReservationCommand reservationCommand);
    void deleteReservation(long id);
    int updateAll(long id, ReservationCommand reservationCommand);
    boolean existsByTimeId(long timeId);
    boolean existsByThemeId(long themeId);
    boolean existsByTimeIdAndThemeIdAndDate(long timeId, long themeId, LocalDate date);
}
