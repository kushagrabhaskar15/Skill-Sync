package com.skillsync.connection;

import com.skillsync.dto.ConnectionRequest;
import com.skillsync.dto.ConnectionResponse;
import com.skillsync.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConnectionResponse sendRequest(@Valid @RequestBody ConnectionRequest request) {
        return connectionService.sendRequest(SecurityUtils.currentUserId(), request);
    }

    @GetMapping
    public List<ConnectionResponse> getConnections() {
        return connectionService.getConnections(SecurityUtils.currentUserId());
    }

    @GetMapping("/requests")
    public List<ConnectionResponse> getPendingRequests() {
        return connectionService.getPendingRequests(SecurityUtils.currentUserId());
    }

    @PatchMapping("/{id}/accept")
    public ConnectionResponse accept(@PathVariable Long id) {
        return connectionService.acceptRequest(SecurityUtils.currentUserId(), id);
    }

    @PatchMapping("/{id}/reject")
    public ConnectionResponse reject(@PathVariable Long id) {
        return connectionService.rejectRequest(SecurityUtils.currentUserId(), id);
    }

    @PatchMapping("/{id}/cancel")
    public ConnectionResponse cancel(@PathVariable Long id) {
        return connectionService.cancelRequest(SecurityUtils.currentUserId(), id);
    }
}
