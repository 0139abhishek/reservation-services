package com.cctech.reserverooms.repo;

import com.cctech.reserverooms.model.MeetingRoom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingRoomRepository extends CrudRepository<MeetingRoom, Long> {

    Optional<MeetingRoom> findByName(String name);

}



