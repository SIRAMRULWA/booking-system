package com.booking.bookingsystem.entity;

import com.booking.bookingsystem.enums.ResourceMode;
import com.booking.bookingsystem.enums.ResourceStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "resources", indexes = {
        @Index(columnList = "resourceCode", unique = true),
        @Index(columnList = "category"),
        @Index(columnList = "status"),
        @Index(columnList = "mode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String resourceCode;

    @Column(nullable = false, length = 50)
    private String category;

    private String location;
    private Integer capacity;
    private Double price;

    @Column(length = 500)
    private String description;

    @ElementCollection
    @CollectionTable(name = "resource_features", joinColumns = @JoinColumn(name = "resource_id"))
    @Column(name = "feature")
    @Builder.Default
    private List<String> features = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ResourceStatus status = ResourceStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ResourceMode mode = ResourceMode.FLEXIBLE;

    // FIXED mode only fields (null for FLEXIBLE)
    private LocalTime slotStart;
    private LocalTime slotEnd;
    private LocalDate slotDate;
    private String recurrence;  // DAILY, WEEKLY, MONTHLY

    @OneToMany(mappedBy = "resource")
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @Version
    private Integer version;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}