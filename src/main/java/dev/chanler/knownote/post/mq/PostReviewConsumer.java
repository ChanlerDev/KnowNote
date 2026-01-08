package dev.chanler.knownote.post.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 发布审核消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "post-review",
        consumerGroup = "knownote-consumer"
)
public class PostReviewConsumer implements RocketMQListener<PostReviewMessage> {

    private final PostReviewService postReviewService;

    @Override
    public void onMessage(PostReviewMessage message) {
        log.info("审核消息: postId={}", message.getPostId());
        try {
            postReviewService.handle(message);
        } catch (Exception e) {
            log.error("审核处理失败: postId={}", message.getPostId(), e);
            throw e; // 抛出异常触发重试
        }
    }
}
