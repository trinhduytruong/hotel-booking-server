package com.example.hotelbookingserver.controller;


import com.example.hotelbookingserver.exception.PhotoRetrievalException;
import com.example.hotelbookingserver.exception.ResourceNotFoundException;
import com.example.hotelbookingserver.model.BookedRoom;
import com.example.hotelbookingserver.model.Room;
import com.example.hotelbookingserver.response.BookingResponse;
import com.example.hotelbookingserver.response.RoomResponse;
import com.example.hotelbookingserver.service.BookingService;
import com.example.hotelbookingserver.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {
    private static final Logger log = LoggerFactory.getLogger(RoomController.class);
    private final IRoomService roomService;
    private final BookingService bookingService;

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


    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        log.info("#### RECEIVE REQUEST ( getAllRooms ) ####");
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();
        for(Room room : rooms){
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if(photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }

        log.info("#### FINISH REQUEST ( getAllRooms ) ####");
        return ResponseEntity.ok(roomResponses);
    }

    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());
        List<BookingResponse> bookingInfo = bookings != null ? bookings.stream().map(booking -> new BookingResponse(
                booking.getBookingId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getBookingConfirmationCode())).toList() : Collections.emptyList();
        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if(photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException e) {
                throw new PhotoRetrievalException("Error retrieving photo");
            }
        }
        return new RoomResponse(room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes,
                bookingInfo);
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);
    }

    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable  Long roomId) {
        log.info("#### RECEIVE REQUEST ( deleteRoom ) ####");
        roomService.deleteRoom(roomId);
        log.info("#### FINISH REQUEST ( deleteRoom ) ####");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @RequestParam(required = false) String roomType,
                                                   @RequestParam(required = false) BigDecimal roomPrice,
                                                   @RequestParam(required = false) MultipartFile photo) throws SQLException, IOException {
        log.info("#### RECEIVE REQUEST ( updateRoom ) ####");
        byte[] photoBytes = photo != null && !photo.isEmpty() ?
                photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);
        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ? new SerialBlob(photoBytes) : null;
        Room theRoom = roomService.upgateRoom(roomId, roomType, roomPrice, photoBytes);
        theRoom.setPhoto(photoBlob);
        RoomResponse roomResponse = getRoomResponse(theRoom);
        log.info("#### FINISH REQUEST ( updateRoom ) ####");
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId) {
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse roomResponse = getRoomResponse((room));
            return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(() -> new ResourceNotFoundException("Room not found!"));
    }

}
