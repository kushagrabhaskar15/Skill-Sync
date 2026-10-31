package com.skillsync.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
            SELECT DISTINCT c FROM Conversation c
            JOIN c.participants p
            WHERE p.user.id = :userId
            ORDER BY c.updatedAt DESC
            """)
    List<Conversation> findByParticipantsUserId(@Param("userId") Long userId);

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.id IN (
                SELECT p1.conversation.id FROM ConversationParticipant p1
                JOIN ConversationParticipant p2 ON p1.conversation.id = p2.conversation.id
                WHERE p1.user.id = :userA AND p2.user.id = :userB
            )
            """)
    Optional<Conversation> findPrivateConversationBetween(
            @Param("userA") Long userA,
            @Param("userB") Long userB
    );
}
