package com.blck.MusicReleaseTracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SSEController {
    private static SseEmitter emitter;

    @GetMapping("/progress")
    public SseEmitter eventStream() {
        //timeout 5min
        emitter = new SseEmitter(300000L);
        return emitter;
    }

    public static void sendProgress(double state) {
        try {
            emitter.send(String.valueOf(state));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (state == 1.0)
                emitter.complete();
        }
    }

}
