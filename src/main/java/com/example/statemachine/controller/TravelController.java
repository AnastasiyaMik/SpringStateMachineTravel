package com.example.statemachine.controller;

import static com.example.statemachine.config.StateMachineConfig.USER_ID_HEADER;

import com.example.statemachine.travel.TravelEvent;
import com.example.statemachine.travel.TravelState;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("travel")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TravelController {

  private final StateMachineService<TravelState, TravelEvent> machineService;

  @GetMapping("/currentState/{userId}")
  public Mono<TravelState> getState(@PathVariable final String userId) {
    return getMachine(userId).map(sm -> sm.getState().getId());
  }

  @PostMapping("/event/{userId}/{event}")
  public Flux<TravelState> sentEvent(@PathVariable final String userId,
      @PathVariable("event") final TravelEvent event) {

    final Message<TravelEvent> eventMessage = MessageBuilder
        .withPayload(event)
        .setHeader(USER_ID_HEADER, userId)
        .build();

    return getMachine(userId).flatMapMany(sm -> sm.sendEvent(Mono.just(eventMessage)))
        .map(result -> result.getRegion().getState().getId());
  }

  private Mono<StateMachine<TravelState, TravelEvent>> getMachine(final String id) {
    return Mono.just(id)
        .publishOn(Schedulers.boundedElastic())
        .map(machineService::acquireStateMachine);
  }
}
