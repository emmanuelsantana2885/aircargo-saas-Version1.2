package com.aircargo.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "mawb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mawb {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "awb_number", length = 50, nullable = false)
    private String awbNumber;
}