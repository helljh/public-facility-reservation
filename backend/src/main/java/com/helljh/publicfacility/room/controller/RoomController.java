package com.helljh.publicfacility.room.controller;

import com.helljh.publicfacility.room.dto.RoomDetailResponse;
import com.helljh.publicfacility.room.dto.RoomResponse;
import com.helljh.publicfacility.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<RoomResponse> getRooms() {
        return roomService.getRooms();
    }

    @GetMapping("/{roomId}")
    public RoomDetailResponse getRoom(
            @PathVariable Long roomId
    ) {
        return roomService.getRoom(roomId);
    }
}