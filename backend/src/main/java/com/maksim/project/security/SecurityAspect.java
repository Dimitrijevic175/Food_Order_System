package com.maksim.project.security;

import com.maksim.project.model.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class SecurityAspect {

    @Pointcut("@annotation(com.maksim.project.security.CheckSecurity)")
    public void checkSecurityPointcut() {}

    @Before("checkSecurityPointcut() && @annotation(checkSecurity)")
    public void checkPermissions(JoinPoint joinPoint, CheckSecurity checkSecurity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Unauthorized");
        }

        Set<String> requiredPermissions = Set.of(checkSecurity.permissions());

        @SuppressWarnings("unchecked")
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();
        Set<String> userPermissions = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (!userPermissions.containsAll(requiredPermissions)) {
            throw new AccessDeniedException(checkSecurity.message());
        }
    }


    @Pointcut("@annotation(com.maksim.project.security.Admin)")
    public void adminPointcut() {}

    @Before("@annotation(com.maksim.project.security.Admin)")
    public void checkAdmin(JoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        // Check the authorities of the authenticated user
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new SecurityException("User does not have admin privileges");
        }
    }

}
