package com.example.statemachine.action;

import static com.example.statemachine.config.StateMachineConfig.USER_ID_HEADER;

import com.example.statemachine.notification.NotificationService;
import com.example.statemachine.travel.TravelEvent;
import com.example.statemachine.travel.TravelState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ErrorAction implements Action<TravelState, TravelEvent> {

  @Override
  public void execute(StateContext<TravelState, TravelEvent> context) {
    log.info("Error action process");

    final var exception = context.getException();
    if (exception != null) {
      log.error("Error handling action: {}", exception.getMessage());
    }

    final Message<TravelEvent> eventMessage = MessageBuilder
        .withPayload(TravelEvent.FINISH_TRAVEL)
        .setHeader(USER_ID_HEADER, context.getMessageHeader(USER_ID_HEADER))
        .build();
    context.getExtendedState().getVariables().put("error", true);
    context.getStateMachine().sendEvent(Mono.just(eventMessage)).subscribe();
  }

}
