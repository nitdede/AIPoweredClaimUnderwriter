package com.ai.claim.underwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Table(name = "helpdesk_tickets")
@NoArgsConstructor
@AllArgsConstructor
public class HelpDeskTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private String issueDescription;
    private String status;
    private String priority;
    private String assignedTo;
    private LocalDateTime created;
    private LocalDateTime eta;

}
