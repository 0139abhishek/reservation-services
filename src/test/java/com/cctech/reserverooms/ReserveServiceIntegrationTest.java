package com.cctech.reserverooms;

import com.cctech.reserverooms.model.Booking;
import com.cctech.reserverooms.model.Employee;
import com.cctech.reserverooms.model.MeetingRoom;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ReserveServiceIntegrationTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldCreateMeetingRoom() {
        final MeetingRoom inputRoom = new MeetingRoom();
        inputRoom.setName("Meeting Room-1");

        final ResponseEntity<MeetingRoom> response = restTemplate.postForEntity("/room", inputRoom, MeetingRoom.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(inputRoom.getName());
    }

    @Test
    public void shouldCreateEmployee() {
        Employee inputEmployee = new Employee();
        inputEmployee.setFirstName("first");
        inputEmployee.setMiddleName("middle");
        inputEmployee.setLastName("last");

        final ResponseEntity<Employee> response = restTemplate.postForEntity("/employee", inputEmployee, Employee.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(inputEmployee.getName());
    }

    @Test
    @Sql(scripts = {
            "/scripts/meeting-room-1.sql",
            "/scripts/employee-1.sql",
            "/scripts/employee-2.sql",
    })
    public void shouldCreateBooking() {
        final MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(171L);
        meetingRoom.setName("meeting-room-1");
        final Employee employee = new Employee();
        employee.setId(31L);
        final Booking aBooking = new Booking("newBooking", meetingRoom, employee, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        final ResponseEntity<Booking> response = restTemplate.postForEntity("/booking", aBooking, Booking.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo(aBooking.getTitle());
    }

    @Test
    @Sql(scripts = {
            "/scripts/meeting-room-1.sql",
            "/scripts/employee-1.sql",
            "/scripts/employee-2.sql",
            "/scripts/booking-1-CreatedBy-employee-1-For-meeting-room-1.sql",
    })
    public void shouldNotAllowBookingIfThereAnyExistingBooking() {
        final Employee employee = new Employee();
        employee.setId(31L);
        final MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(171L);

        final Booking bookingWithSameDurationAsExisting = new Booking("anotherBooking", meetingRoom, employee, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        final ResponseEntity<String> response = restTemplate.postForEntity("/booking", bookingWithSameDurationAsExisting, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql(scripts = {
            "/scripts/meeting-room-1.sql",
            "/scripts/employee-1.sql",
            "/scripts/employee-2.sql",
            "/scripts/booking-1-CreatedBy-employee-1-For-meeting-room-1.sql",
            "/scripts/booking-2-CreatedBy-employee-2-For-meeting-room-1.sql",
    })
    public void shouldReturnBookingsForAGivenRoom() {
        final MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setName("meeting-room-1");

        final ResponseEntity<Booking[]> response = restTemplate.getForEntity("/booking/search?roomName=" + meetingRoom.getName(), Booking[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty().hasSize(2);
    }


    @Test
    @Sql(scripts = {
            "/scripts/employee-1.sql",
    })
    public void bookingRequestShouldFailWhenMeetingRoomDoesntExist() {
        final Employee employee = new Employee();
        employee.setId(31L);
        final MeetingRoom withNonExistingRoom = new MeetingRoom();
        withNonExistingRoom.setName("Not yet created");
        final Booking aBooking = new Booking("aBooking", withNonExistingRoom,
                employee, LocalDateTime.now(), LocalDateTime.now().plusHours(1)
        );

        final ResponseEntity<String> response = restTemplate.getForEntity("/booking/search?roomName=" + withNonExistingRoom.getName(), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotEmpty();
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(con, new ClassPathResource("/scripts/cleanup.sql"));
        }
    }
}
