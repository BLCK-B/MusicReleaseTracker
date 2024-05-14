package com.blck.MusicReleaseTracker.FrontendAPI;

import com.blck.MusicReleaseTracker.Core.ErrorLogging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/*      MusicReleaseTracker
        Copyright (C) 2023 BLCK
        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.
        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.
        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

/** server side event controller for progressbar */
@RestController
public class SSEController {

    private final ErrorLogging log;
    private SseEmitter emitter;

    @Autowired
    public SSEController(ErrorLogging errorLogging) {
        this.log = errorLogging;
    }

    @GetMapping("/progress")
    public SseEmitter eventStream() {
        emitter = new SseEmitter(300000L);
        return emitter;
    }

    public void sendProgress(double state) {
        try {
            emitter.send(String.valueOf(state));
        } catch (Exception e) {
            log.error(e, ErrorLogging.Severity.WARNING, "error in progress emitter");
        } finally {
            if (state == 1.0)
                emitter.complete();
        }
    }

}
