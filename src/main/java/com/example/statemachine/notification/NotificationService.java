package com.example.statemachine.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationService {

  public void send() {
    log.info("sending notification");
  }

  public void call() {
    log.info("calling home");
  }
}
