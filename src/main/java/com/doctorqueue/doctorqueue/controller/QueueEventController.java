package com.doctorqueue.doctorqueue.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/queue")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:3000"
})
public class QueueEventController {

    private final List<SseEmitter> emitters =
            new CopyOnWriteArrayList<>();

    @GetMapping(
            value = "/events",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribe() {

        SseEmitter emitter =
                new SseEmitter(0L);

        emitters.add(emitter);

        emitter.onCompletion(
                () -> emitters.remove(emitter)
        );

        emitter.onTimeout(
                () -> emitters.remove(emitter)
        );

        emitter.onError(
                error -> emitters.remove(emitter)
        );

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data("Connected to queue updates")
            );
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void publish(String event, Object data) {

        for (SseEmitter emitter : emitters) {

            try {

                emitter.send(
                        SseEmitter.event()
                                .name(event)
                                .data(data)
                );

            } catch (IOException e) {

                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}
