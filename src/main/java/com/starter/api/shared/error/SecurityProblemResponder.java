package com.starter.api.shared.error;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

public final class SecurityProblemResponder {

    private SecurityProblemResponder() {
    }

    public static AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper mapper) {
        return (_, response, _) ->
                write(mapper, response, ErrorCode.UNAUTHENTICATED, "Authentication is required");
    }

    public static AccessDeniedHandler accessDeniedHandler(ObjectMapper mapper) {
        return (_, response, _) ->
                write(mapper, response, ErrorCode.FORBIDDEN, "Access is denied");
    }

    private static void write(ObjectMapper mapper, HttpServletResponse response, ErrorCode code, String detail)
            throws IOException {
        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", code.status().getReasonPhrase());
        body.put("status", code.status().value());
        body.put("detail", detail);
        body.put("code", code.code());
        mapper.writeValue(response.getOutputStream(), body);
    }
}
