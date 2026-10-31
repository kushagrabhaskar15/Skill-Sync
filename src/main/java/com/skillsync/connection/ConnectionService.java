package com.skillsync.connection;

import com.skillsync.chat.ChatService;
import com.skillsync.common.MapperUtils;
import com.skillsync.dto.ConnectionRequest;
import com.skillsync.dto.ConnectionResponse;
import com.skillsync.enums.ConnectionStatus;
import com.skillsync.enums.RecommendationStatus;
import com.skillsync.exception.BadRequestException;
import com.skillsync.exception.ConflictException;
import com.skillsync.exception.ForbiddenException;
import com.skillsync.exception.ResourceNotFoundException;
import com.skillsync.recommendation.RecommendationRepository;
import com.skillsync.user.User;
import com.skillsync.user.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserService userService;
    private final ChatService chatService;
    private final RecommendationRepository recommendationRepository;

    public ConnectionService(
            ConnectionRepository connectionRepository,
            UserService userService,
            @Lazy ChatService chatService,
            RecommendationRepository recommendationRepository
    ) {
        this.connectionRepository = connectionRepository;
        this.userService = userService;
        this.chatService = chatService;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional
    public ConnectionResponse sendRequest(Long userId, ConnectionRequest request) {
        if (userId.equals(request.receiverId())) {
            throw new BadRequestException("Cannot send a connection request to yourself");
        }

        User requester = userService.requireUser(userId);
        User receiver = userService.requireUser(request.receiverId());

        boolean exists = connectionRepository.existsBetweenUsersWithStatuses(
                userId,
                request.receiverId(),
                List.of(ConnectionStatus.PENDING, ConnectionStatus.ACCEPTED)
        );
        if (exists) {
            throw new ConflictException("A pending or accepted connection already exists");
        }

        Connection connection = Connection.builder()
                .requester(requester)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .message(request.message())
                .build();

        return toResponse(connectionRepository.save(connection));
    }

    @Transactional
    public ConnectionResponse acceptRequest(Long userId, Long connectionId) {
        Connection connection = connectionRepository.findByIdAndReceiverId(connectionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be accepted");
        }

        connection.setStatus(ConnectionStatus.ACCEPTED);
        chatService.createConversationForAcceptedConnection(
                connection.getRequester().getId(),
                connection.getReceiver().getId()
        );

        markRecommendationConnected(connection.getRequester().getId(), connection.getReceiver().getId());
        markRecommendationConnected(connection.getReceiver().getId(), connection.getRequester().getId());

        return toResponse(connection);
    }

    @Transactional
    public ConnectionResponse rejectRequest(Long userId, Long connectionId) {
        Connection connection = connectionRepository.findByIdAndReceiverId(connectionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be rejected");
        }

        connection.setStatus(ConnectionStatus.REJECTED);
        return toResponse(connection);
    }

    @Transactional
    public ConnectionResponse cancelRequest(Long userId, Long connectionId) {
        Connection connection = connectionRepository.findByIdAndRequesterId(connectionId, userId)
                .orElseThrow(() -> new ForbiddenException("Only the requester can cancel this request"));

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be cancelled");
        }

        connection.setStatus(ConnectionStatus.CANCELLED);
        return toResponse(connection);
    }

    @Transactional(readOnly = true)
    public List<ConnectionResponse> getConnections(Long userId) {
        return connectionRepository.findByUserIdAndStatus(userId, ConnectionStatus.ACCEPTED).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConnectionResponse> getPendingRequests(Long userId) {
        return connectionRepository.findByReceiverIdAndStatus(userId, ConnectionStatus.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean areConnected(Long userA, Long userB) {
        return connectionRepository.areConnected(userA, userB);
    }

    private void markRecommendationConnected(Long userId, Long recommendedUserId) {
        recommendationRepository.findByUserIdOrderByGeneratedAtDesc(userId).stream()
                .filter(r -> r.getRecommendedUser().getId().equals(recommendedUserId))
                .findFirst()
                .ifPresent(r -> r.setStatus(RecommendationStatus.CONNECTED));
    }

    private ConnectionResponse toResponse(Connection connection) {
        return new ConnectionResponse(
                connection.getId(),
                MapperUtils.toUserSummary(connection.getRequester()),
                MapperUtils.toUserSummary(connection.getReceiver()),
                connection.getStatus(),
                connection.getMessage(),
                connection.getCreatedAt(),
                connection.getUpdatedAt()
        );
    }
}
