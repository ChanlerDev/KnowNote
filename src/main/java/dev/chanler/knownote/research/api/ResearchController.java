package dev.chanler.knownote.research.api;

import dev.chanler.knownote.common.Result;
import dev.chanler.knownote.common.UserContext;
import dev.chanler.knownote.common.sse.SseHub;
import dev.chanler.knownote.research.api.dto.req.SendMessageReqDTO;
import dev.chanler.knownote.research.api.dto.resp.CreateResearchRespDTO;
import dev.chanler.knownote.research.api.dto.resp.ResearchMessageRespDTO;
import dev.chanler.knownote.research.api.dto.resp.ResearchStatusRespDTO;
import dev.chanler.knownote.research.api.dto.resp.SendMessageRespDTO;
import dev.chanler.knownote.research.service.ResearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 研究模块接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/research")
public class ResearchController {

    private final ResearchService researchService;
    private final SseHub sseHub;

    @GetMapping("/create")
    public Result<CreateResearchRespDTO> createResearch(@RequestParam Integer num) {
        return Result.ok(researchService.createResearch(num));
    }

    @GetMapping("/list")
    public Result<List<ResearchStatusRespDTO>> getResearchList() {
        return Result.ok(researchService.getResearchList());
    }

    @GetMapping("/{researchId}")
    public Result<ResearchStatusRespDTO> getResearchStatus(@PathVariable String researchId) {
        return Result.ok(researchService.getResearchStatus(researchId));
    }

    @GetMapping("/{researchId}/messages")
    public Result<ResearchMessageRespDTO> getResearchMessages(@PathVariable String researchId) {
        return Result.ok(researchService.getResearchMessages(researchId));
    }

    @PostMapping("/{researchId}/messages")
    public Result<SendMessageRespDTO> sendMessage(
            @PathVariable String researchId,
            @RequestBody SendMessageReqDTO sendMessageReqDTO) {
        return Result.ok(researchService.sendMessage(researchId, sendMessageReqDTO));
    }

    @GetMapping("/sse")
    public SseEmitter stream(
            @RequestHeader("X-Research-Id") String researchId,
            @RequestHeader("X-Client-Id") String clientId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        Long userId = UserContext.getUserId();
        return sseHub.connect(userId, researchId, clientId, lastEventId);
    }
}
