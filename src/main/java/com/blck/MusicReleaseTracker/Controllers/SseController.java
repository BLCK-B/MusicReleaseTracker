
package com.blck.MusicReleaseTracker.Controllers;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * server side event controller for progressbar
 */
@RestController
public class SseController {

    private final ErrorLogging log;
    private SseEmitter emitter;

    @Autowired
    public SseController(ErrorLogging errorLogging) {
        this.log = errorLogging;
    }

    @GetMapping("/progress")
    public SseEmitter eventStream() {
        emitter = new SseEmitter(3000000L); // timeout 10 min
        return emitter;
    }

    /**
     * Close emitter and conclude progress bar
     */
    public void complete() {
        emitter.complete();
    }

    /**
     * Set frontend progress bar value
     *
     * @param state 0.0 - 1.0
     */
    public void sendProgress(double state) {
        try {
            emitter.send(String.valueOf(state));
        } catch (IllegalStateException e) {
            log.error(e, ErrorLogging.Severity.INFO, "SSE progress controller called while being unopened");
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error in progress emitter");
        }
    }

}
