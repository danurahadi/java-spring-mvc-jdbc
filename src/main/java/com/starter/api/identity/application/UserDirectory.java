package com.starter.api.identity.application;

import com.starter.api.identity.domain.User;
import java.util.Optional;

public interface UserDirectory {

    Optional<User> find(long userId);
}
