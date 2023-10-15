package com.todolist.todolist.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.todolist.todolist.user.IUserRepository;
import com.todolist.todolist.user.UserModel;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    private static final String TASKS_PATH = "/tasks/";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var serverletPath = request.getServletPath();

        if (serverletPath.startsWith(TASKS_PATH)) {
            String authorization = request.getHeader(AUTHORIZATION_HEADER);

            String authEncoded = authorization.substring("Basic".length()).trim();

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded.getBytes(StandardCharsets.UTF_8));

            try (ByteArrayInputStream stream = new ByteArrayInputStream(authDecoded)) {

                String authString = new String(authDecoded);

                String[] credentials = authString.split(":");

                if (credentials.length != 2) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                String username = credentials[0];
                String password = credentials[1];

                UserModel user = this.userRepository.findByUsername(username);

                if (user == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                    if (passwordVerify.verified) {
                        request.setAttribute("idUser", user.getId());
                        filterChain.doFilter(request, response);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        } else {
            filterChain.doFilter(request, response);
        }

    }
}
