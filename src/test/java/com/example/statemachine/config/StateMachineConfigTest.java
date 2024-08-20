package com.example.statemachine.config;


import static com.example.statemachine.config.StateMachineConfig.USER_ID_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.statemachine.action.SendRequestNotificationForDocumentsAction;
import com.example.statemachine.travel.TravelEvent;
import com.example.statemachine.travel.TravelState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Slf4j
class StateMachineConfigTest {

  @Mock
  private SendRequestNotificationForDocumentsAction sendRequestNotificationForDocumentsAction;

  @Autowired
  private StateMachineFactory<TravelState, TravelEvent> stateMachineFactory;

  @Test
  void testInitialState() {
    StateMachine<TravelState, TravelEvent> stateMachine = stateMachineFactory.getStateMachine();
    stateMachine.start();
    assertEquals(TravelState.PLANNING, stateMachine.getState().getId());
  }

  @Test
  void testStateTransition() {
    StateMachine<TravelState, TravelEvent> stateMachine = stateMachineFactory.getStateMachine("test");
    stateMachine.start();

    stateMachine.sendEvent(TravelEvent.DESTINATION_CHOSEN);
    assertTrue(TravelState.DOCUMENTS_READY.equals(stateMachine.getState().getId()) ||
        TravelState.WAITING_DOCUMENTS.equals(stateMachine.getState().getId()));
  }

  @Test
  void testErrorStateTransition() {
    StateMachine<TravelState, TravelEvent> ssm = stateMachineFactory.getStateMachine("test");
    ssm.getExtendedState().getVariables().put("error", true);
    ssm.startReactively().block();

    ssm.sendEvent(
            Mono.just(MessageBuilder.withPayload(TravelEvent.FINISH_TRAVEL).setHeader(USER_ID_HEADER, "test").build()))
        .subscribe(r -> log.info("event {}, {}", r.getResultType(), ssm.getState()));

    assertThat(ssm.getState().getId(), is(TravelState.COMPLETION));
  }

  @Test
  void testAllStateTransitions() throws Exception {
    final StateMachine<TravelState, TravelEvent> stateMachine = stateMachineFactory.getStateMachine("test");

    final StateMachineTestPlan<TravelState, TravelEvent> plan = StateMachineTestPlanBuilder.<TravelState, TravelEvent>builder()
        .stateMachine(stateMachine)
        .step()
        .expectStates(TravelState.PLANNING)
        .and()
        .step()
        .sendEvent(TravelEvent.DESTINATION_CHOSEN)
        .sendEvent(TravelEvent.DOCUMENTS_PREPARED)
        .sendEvent(TravelEvent.TRANSPORT_BOOKED)
        .expectState(TravelState.BOOK_ACCOMMODATION)
        .and()
        .step()
        .sendEvent(TravelEvent.ACCOMMODATION_BOOKED)
        .expectState(TravelState.PACK_LUGGAGE)
        .and()
        .step()
        .sendEvent(TravelEvent.SEND_POST)
        .expectState(TravelState.PACK_LUGGAGE)
        .expectStateChanged(0)
        .and()
        .step()
        .sendEvent(TravelEvent.READY_FOR_TRIP)
        .expectState(TravelState.TRAVELING)
        .and()
        .step()
        .sendEvent(TravelEvent.RETURN_HOME)
        .expectState(TravelState.COMPLETION)
        .and()
        .build();

    plan.test();
  }
}
