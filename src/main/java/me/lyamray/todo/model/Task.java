package me.lyamray.todo.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    private Long id;
    private String title;
    private String description;
    @Builder.Default
    private String status = "Not Started";
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
