package com.skillsync.chat;

import com.skillsync.common.MapperUtils;
import com.skillsync.connection.ConnectionService;
import com.skillsync.dto.*;
import com.skillsync.enums.MessageType;
import com.skillsync.exception.BadRequestException;
import com.skillsync.exception.ForbiddenException;
import com.skillsync.exception.ResourceNotFoundException;
import com.skillsync.user.User;
import com.skillsync.user.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ConnectionService connectionService;

    public ChatService(
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            MessageRepository messageRepository,
            UserService userService,
            @Lazy ConnectionService connectionService
    ) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.connectionService = connectionService;
    }

    @Transactional
    public ConversationResponse createConversation(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BadRequestException("Cannot create a conversation with yourself");
        }
        if (!connectionService.areConnected(userId, otherUserId)) {
            throw new ForbiddenException("Chat is available only after a connection is accepted");
        }
        return toConversationResponse(getOrCreatePrivateConversation(userId, otherUserId), userId);
    }

    @Transactional
    public Conversation createConversationForAcceptedConnection(Long userA, Long userB) {
        return getOrCreatePrivateConversation(userA, userB);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(Long userId) {
        return conversationRepository.findByParticipantsUserId(userId).stream()
                .map(conversation -> toConversationResponse(conversation, userId))
                .toList();
    }

    @Transactional
    public MessageResponse sendMessage(Long userId, Long conversationId, SendMessageRequest request) {
        requireParticipant(conversationId, userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        User sender = userService.requireUser(userId);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.content().trim())
                .messageType(request.messageType() == null ? MessageType.TEXT : request.messageType())
                .isDeleted(false)
                .build();

        messageRepository.save(message);
        conversation.setUpdatedAt(LocalDateTime.now());

        ConversationParticipant senderParticipant = participantRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow();
        senderParticipant.setLastReadAt(LocalDateTime.now());

        return MapperUtils.toMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long userId, Long conversationId, Pageable pageable) {
        requireParticipant(conversationId, userId);
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId, pageable)
                .map(MapperUtils::toMessageResponse);
    }

    @Transactional
    public void markRead(Long userId, Long conversationId) {
        ConversationParticipant participant = participantRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException("Not a conversation participant"));
        participant.setLastReadAt(LocalDateTime.now());
    }

    private Conversation getOrCreatePrivateConversation(Long userA, Long userB) {
        return conversationRepository.findPrivateConversationBetween(userA, userB)
                .orElseGet(() -> {
                    Conversation conversation = conversationRepository.save(Conversation.builder().build());
                    User first = userService.requireUser(userA);
                    User second = userService.requireUser(userB);
                    participantRepository.save(ConversationParticipant.builder()
                            .conversation(conversation)
                            .user(first)
                            .build());
                    participantRepository.save(ConversationParticipant.builder()
                            .conversation(conversation)
                            .user(second)
                            .build());
                    return conversation;
                });
    }

    private void requireParticipant(Long conversationId, Long userId) {
        if (!participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new ForbiddenException("Only conversation participants can access messages");
        }
    }

    private ConversationResponse toConversationResponse(Conversation conversation, Long currentUserId) {
        List<ConversationParticipant> participants = participantRepository.findByConversationId(conversation.getId());
        User other = participants.stream()
                .map(ConversationParticipant::getUser)
                .filter(user -> !user.getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Conversation participant not found"));

        ConversationParticipant me = participants.stream()
                .filter(p -> p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow();

        MessageResponse lastMessage = messageRepository
                .findFirstByConversationIdAndIsDeletedFalseOrderBySentAtDesc(conversation.getId())
                .map(MapperUtils::toMessageResponse)
                .orElse(null);

        long unread = messageRepository.countUnread(conversation.getId(), currentUserId, me.getLastReadAt());

        return new ConversationResponse(
                conversation.getId(),
                MapperUtils.toUserSummary(other),
                lastMessage,
                unread,
                conversation.getUpdatedAt()
        );
    }
}
