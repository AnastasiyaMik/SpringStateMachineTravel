package com.example.statemachine.config;

import com.example.statemachine.travel.TravelEvent;
import com.example.statemachine.travel.TravelState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

@Configuration
public class StateMachinePersist {

  @Bean
  public StateMachineRuntimePersister<TravelState, TravelEvent, String> stateMachineRuntimePersister(
      JpaStateMachineRepository jpaStateMachineRepository) {
    return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
  }

  @Bean
  public StateMachineService<TravelState, TravelEvent> stateMachineService(
      StateMachineFactory<TravelState, TravelEvent> stateMachineFactory,
      StateMachineRuntimePersister<TravelState, TravelEvent, String> stateMachineRuntimePersister) {
    return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
  }

}
