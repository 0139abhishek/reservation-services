package com.cctech.reserverooms.api;

import com.cctech.reserverooms.error.ReservationServiceBadRequestException;
import com.cctech.reserverooms.model.Booking;
import com.cctech.reserverooms.model.Employee;
import com.cctech.reserverooms.model.MeetingRoom;
import com.cctech.reserverooms.repo.BookingRepository;
import com.cctech.reserverooms.repo.EmployeeRepository;
import com.cctech.reserverooms.repo.MeetingRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReservationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationController.class);
    private final EmployeeRepository employeeRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public ReservationController(EmployeeRepository employeeRepository, MeetingRoomRepository meetingRoomRepository, BookingRepository bookingRepository) {
        this.employeeRepository = employeeRepository;
        this.meetingRoomRepository = meetingRoomRepository;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping("/room")
    public MeetingRoom createRoom(@RequestBody MeetingRoom meetingRoom) {
        meetingRoomRepository.findByName(meetingRoom.getName()).ifPresent(c -> {
            LOGGER.info("Conference room not found");
            throw new ReservationServiceBadRequestException("Conference room '" + meetingRoom.getName() + "' already exists");
        });
        meetingRoom.setId(null);
        LOGGER.info("Creating new conference room");
        return meetingRoomRepository.save(meetingRoom);
    }

    @PostMapping("/employee")
    public Employee createEmployee(@RequestBody Employee employee) {
        employee.setId(null);
        LOGGER.info("Creating new employee");
        return employeeRepository.save(employee);
    }

    @GetMapping("/booking/search")
    public Iterable<Booking> getBookings(@RequestParam String roomName) {
        meetingRoomRepository.findByName(roomName)
                .orElseThrow(() -> new ReservationServiceBadRequestException("Conference room " + roomName + " cannot be found"));
        LOGGER.info("Returning reservations");
        return bookingRepository.findBookingsByMeetingRoomName(roomName);
    }

    @PostMapping("/booking")
    public Booking createBooking(@RequestBody Booking newBooking) {
        newBooking.setId(null);
        getBookings(newBooking.getMeetingRoom().getName()).forEach(existingBooking -> {
            if (isBookingWithInExisting(newBooking, existingBooking)
                    || isBookingStartDateWithInExisting(newBooking, existingBooking)
                    || isBookingEndDateWithInExisting(newBooking, existingBooking)) {
                throw new ReservationServiceBadRequestException("Already an existing booking available for the same schedule");
            }
        });
        return bookingRepository.save(newBooking);
    }

    private boolean isBookingWithInExisting(@RequestBody Booking newBooking, Booking existingBooking) {
        return newBooking.getStartTime().isAfter(existingBooking.getStartTime())
                && newBooking.getEndTime().isBefore(existingBooking.getEndTime());
    }

    private boolean isBookingEndDateWithInExisting(@RequestBody Booking newBooking, Booking existingBooking) {
        return newBooking.getEndTime().isAfter(existingBooking.getStartTime())
                && newBooking.getStartTime().isBefore(existingBooking.getStartTime());
    }

    private boolean isBookingStartDateWithInExisting(@RequestBody Booking newBooking, Booking existingBooking) {
        return newBooking.getStartTime().isBefore(existingBooking.getEndTime())
                && newBooking.getEndTime().isAfter(existingBooking.getEndTime());
    }

    //    @DeleteMapping
//    @RequestMapping("/reserve")
    public void cancelReservation(@RequestBody Booking booking) {
        meetingRoomRepository.findById(booking.getId());
    }


}
