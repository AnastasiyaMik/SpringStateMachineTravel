package com.example.statemachine.action;

import static com.example.statemachine.config.StateMachineConfig.USER_ID_HEADER;

import com.example.statemachine.notification.NotificationService;
import com.example.statemachine.travel.TravelEvent;
import com.example.statemachine.travel.TravelState;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendRequestNotificationForDocumentsAction implements Action<TravelState, TravelEvent> {

  private final NotificationService notificationService;

  @Override
  public void execute(StateContext<TravelState, TravelEvent> stateContext) {
    var user = (String) stateContext.getMessageHeader(USER_ID_HEADER);
    if ("Thug".equalsIgnoreCase(user)) {
        throw new RuntimeException("Error occurred during processing");
      }
      notificationService.send();
  }
}
