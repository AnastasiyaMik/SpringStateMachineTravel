package com.example.statemachine.listener;

import com.example.statemachine.travel.TravelEvent;
import com.example.statemachine.travel.TravelState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StateMachineEventListener extends StateMachineListenerAdapter<TravelState, TravelEvent> {

  @Override
  public void stateChanged(
      final State<TravelState, TravelEvent> from,
      final State<TravelState, TravelEvent> to
  ) {
    log.info("Travel status changed from {} to {}", from != null ? from.getId() : null, to.getId());
  }

  @Override
  public void stateMachineError(
      final StateMachine<TravelState, TravelEvent> stateMachine,
      final Exception exception
  ) {
    log.warn(
        "Travel status failed to change from {} error: {}",
        stateMachine.getState() != null ? stateMachine.getState().getId() : null,
        exception.getMessage()
    );
  }
}
