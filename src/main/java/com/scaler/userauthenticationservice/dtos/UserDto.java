package com.scaler.userauthenticationservice.dtos;

import com.scaler.userauthenticationservice.models.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class UserDto {
    private Long id;
    private String email;
    private List<Role> roles = new ArrayList<>();
}
