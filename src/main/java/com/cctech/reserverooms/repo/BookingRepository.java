package com.cctech.reserverooms.repo;

import com.cctech.reserverooms.model.Booking;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends CrudRepository<Booking, Long> {

    Iterable<Booking> findBookingsByMeetingRoomName(String name);
}
