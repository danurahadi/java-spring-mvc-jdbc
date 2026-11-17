package com.starter.api.identity.application;

import com.starter.api.identity.domain.User;
import com.starter.api.shared.security.CurrentUser;

public record ResolvedUser(CurrentUser current, User user) {
}
