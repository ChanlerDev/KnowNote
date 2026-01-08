package dev.chanler.knownote.post.mq.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResult {
    private Boolean approved;
    private String rejectedReason;
}
