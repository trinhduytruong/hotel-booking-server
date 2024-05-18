package com.example.hotelbookingserver.controller;


import com.example.hotelbookingserver.model.Room;
import com.example.hotelbookingserver.response.RoomResponse;
import com.example.hotelbookingserver.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {
    private static final Logger log = LoggerFactory.getLogger(RoomController.class);
    private final IRoomService roomService;

    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {

        log.info("#### RECEIVE REQUEST ( addNewRoom ) ####");
        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(),
                savedRoom.getRoomPrice());
        log.info("#### FINISH REQUEST ( addNewRoom ) ####");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/room/types")
    public List<String> getRoomTypes() {
        log.info("#### RECEIVE REQUEST ( getRoomTypes ) ####");
        log.info("#### FINISH REQUEST ( getRoomTypes ) ####");
        return roomService.getAllRoomTypes();
    }

}
