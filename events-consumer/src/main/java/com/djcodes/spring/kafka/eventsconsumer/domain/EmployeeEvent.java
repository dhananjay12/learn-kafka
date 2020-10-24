package com.djcodes.spring.kafka.eventsconsumer.domain;

import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EmployeeEvent {

    private Integer employeeEventId;
    private EmployeeEventType employeeEventType;

    @Valid
    private Employee employee;

}
