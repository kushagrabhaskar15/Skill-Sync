package com.skillsync.connection;

import com.skillsync.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findByRequesterIdAndStatus(Long requesterId, ConnectionStatus status);

    List<Connection> findByReceiverIdAndStatus(Long receiverId, ConnectionStatus status);

    Optional<Connection> findByIdAndReceiverId(Long id, Long receiverId);

    Optional<Connection> findByIdAndRequesterId(Long id, Long requesterId);

    @Query("""
            SELECT c FROM Connection c
            WHERE (c.requester.id = :userId OR c.receiver.id = :userId)
              AND c.status = :status
            """)
    List<Connection> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ConnectionStatus status);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Connection c
            WHERE c.status IN :statuses
              AND ((c.requester.id = :userA AND c.receiver.id = :userB)
                OR (c.requester.id = :userB AND c.receiver.id = :userA))
            """)
    boolean existsBetweenUsersWithStatuses(
            @Param("userA") Long userA,
            @Param("userB") Long userB,
            @Param("statuses") List<ConnectionStatus> statuses
    );

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Connection c
            WHERE c.status = com.skillsync.enums.ConnectionStatus.ACCEPTED
              AND ((c.requester.id = :userA AND c.receiver.id = :userB)
                OR (c.requester.id = :userB AND c.receiver.id = :userA))
            """)
    boolean areConnected(@Param("userA") Long userA, @Param("userB") Long userB);
}
