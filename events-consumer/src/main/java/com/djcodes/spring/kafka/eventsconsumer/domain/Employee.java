package com.djcodes.spring.kafka.eventsconsumer.domain;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Employee {

    private Integer id;

    @NotBlank(message = "Name can not be null!")
    private String name;

    @NotBlank(message = "Title can not be null!")
    private String title;

    @NotBlank(message = "Email can not be null!")
    @Email(message = "Invalid Email")
    private String email;

}
