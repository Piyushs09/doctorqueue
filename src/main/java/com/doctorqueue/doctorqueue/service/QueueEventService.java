
package com.doctorqueue.doctorqueue.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QueueEventService {

    private final List<SseEmitter> emitters =
            new CopyOnWriteArrayList<>();

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
                            .data("Queue event connection established")
            );

        } catch (IOException e) {

            emitters.remove(emitter);
        }

        return emitter;
    }

    public void publishQueueUpdate() {

        for (SseEmitter emitter : emitters) {

            try {

                emitter.send(
                        SseEmitter.event()
                                .name("queue-updated")
                                .data("Queue updated")
                );

            } catch (IOException e) {

                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}