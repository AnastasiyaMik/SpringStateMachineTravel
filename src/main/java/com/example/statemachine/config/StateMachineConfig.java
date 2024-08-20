package com.example.statemachine.config;

import com.example.statemachine.action.ErrorAction;
import com.example.statemachine.action.SendRequestNotificationForDocumentsAction;
import com.example.statemachine.listener.StateMachineEventListener;
import com.example.statemachine.travel.TravelEvent;
import com.example.statemachine.travel.TravelState;
import java.util.EnumSet;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class StateMachineConfig extends StateMachineConfigurerAdapter<TravelState, TravelEvent> {

  public static final String USER_ID_HEADER = "USER_ID_HEADER";

  private final StateMachineRuntimePersister<TravelState, TravelEvent, String> stateMachineRuntimePersister;

  private final StateMachineEventListener machineEventListener;

  private final SendRequestNotificationForDocumentsAction sendRequestNotificationForDocumentsAction;
  private final ErrorAction errorAction;

  @Override
  @SneakyThrows
  public void configure(final StateMachineStateConfigurer<TravelState, TravelEvent> states) {
    states.withStates()
        .initial(TravelState.PLANNING)
        .choice(TravelState.PAPERWORK)
        .junction(TravelState.CHOOSE_TRANSPORT)
        .states(EnumSet.allOf(TravelState.class))
        .end(TravelState.COMPLETION);
  }

  @Override
  public void configure(final StateMachineTransitionConfigurer<TravelState, TravelEvent> transitions) throws Exception {

    transitions
        .withExternal()
        .source(TravelState.PLANNING)
        .target(TravelState.PAPERWORK)
        .event(TravelEvent.DESTINATION_CHOSEN)
        .and()

        .withChoice()
        .source(TravelState.PAPERWORK)
        .first(TravelState.WAITING_DOCUMENTS, visaCheckGuard(), sendRequestNotificationForDocumentsAction,
            errorAction)
        .last(TravelState.DOCUMENTS_READY)
        .and()

        .withExternal()
        .source(TravelState.PLANNING)
        .target(TravelState.COMPLETION)
        .guard(errorGuard())
        .event(TravelEvent.FINISH_TRAVEL)
        .and()

        .withExternal()
        .source(TravelState.WAITING_DOCUMENTS)
        .target(TravelState.CHOOSE_TRANSPORT)
        .event(TravelEvent.DOCUMENTS_PREPARED)
        .and()

        .withExternal()
        .source(TravelState.DOCUMENTS_READY)
        .target(TravelState.CHOOSE_TRANSPORT)
        .and()

        .withJunction()
        .source(TravelState.CHOOSE_TRANSPORT)
        .first(TravelState.BOOK_PLANE, travelContext -> transportChosenPlane())
        .then(TravelState.BOOK_TRAIN, travelContext -> transportChosenTrain())
        .last(TravelState.BOOK_CAR)
        .and()

        .withExternal()
        .source(TravelState.BOOK_PLANE)
        .target(TravelState.BOOK_ACCOMMODATION)
        .event(TravelEvent.TRANSPORT_BOOKED)
        .and()

        .withExternal()
        .source(TravelState.BOOK_TRAIN)
        .target(TravelState.BOOK_ACCOMMODATION)
        .event(TravelEvent.TRANSPORT_BOOKED)
        .and()

        .withExternal()
        .source(TravelState.BOOK_CAR)
        .target(TravelState.BOOK_ACCOMMODATION)
        .event(TravelEvent.TRANSPORT_BOOKED)
        .and()

        .withExternal()
        .source(TravelState.BOOK_ACCOMMODATION)
        .target(TravelState.PACK_LUGGAGE)
        .event(TravelEvent.ACCOMMODATION_BOOKED)
        .and()

        .withInternal()
        .source(TravelState.PACK_LUGGAGE)
        .event(TravelEvent.SEND_POST)
        .action(postAction())
        .and()

        .withExternal()
        .source(TravelState.PACK_LUGGAGE)
        .target(TravelState.TRAVELING)
        .event(TravelEvent.READY_FOR_TRIP)
        .and()

        .withExternal()
        .source(TravelState.TRAVELING)
        .target(TravelState.COMPLETION)
        .event(TravelEvent.RETURN_HOME)
    ;
  }

  @Override
  @SneakyThrows
  public void configure(final StateMachineConfigurationConfigurer<TravelState, TravelEvent> config) {
    config.withConfiguration().listener(machineEventListener);
    config.withPersistence().runtimePersister(stateMachineRuntimePersister);
  }

  @Bean
  public Guard<TravelState, TravelEvent> visaCheckGuard() {
    return context -> {
      var user = (String) context.getMessageHeader(USER_ID_HEADER);
      if ("Thug".equalsIgnoreCase(user) || "Anna".equalsIgnoreCase(user)) {
        return true;
      }

      var random = new Random();
      var result = random.nextBoolean();
      log.info("Visa check result: {}", result);
      return result;
    };
  }

  @Bean
  public Guard<TravelState, TravelEvent> errorGuard() {
    log.info("ErrorGuard");
    return context -> Boolean.TRUE.equals(context.getExtendedState().getVariables().get("error"));
  }

  @Bean
  public Action<TravelState, TravelEvent> postAction() {
    return context -> log.info("Sending happy photo to the chat with friends ;)");
  }

  private boolean transportChosenPlane() {
    final Random random = new Random();
    boolean result = random.nextBoolean();
    log.info("Chosen plane result: {}", result);
    return result;
  }

  private boolean transportChosenTrain() {
    final Random random = new Random();
    boolean result = random.nextBoolean();
    log.info("Chosen train result: {}", result);
    return result;
  }

}
