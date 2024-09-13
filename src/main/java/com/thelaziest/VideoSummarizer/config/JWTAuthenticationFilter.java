package com.thelaziest.VideoSummarizer.config;

import com.thelaziest.VideoSummarizer.service.JWTService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;

    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    public JWTAuthenticationFilter(JWTService jwtService, UserDetailsService userDetailsService, HandlerExceptionResolver handlerExceptionResolver) {
        System.out.println("JWTAuthenticationFilter init.");
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        System.out.println("JWT filter applied for: " + request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");

        // If the header is missing or doesn't start with "Bearer ", we should not continue processing
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continue without setting authentication
            return;
        }

        try{
            System.out.println("authorizationHeader " + authorizationHeader);

            final String jwtToken = authorizationHeader.substring(7);

            // validate token before extract
//            if(!jwtService.isTokenSignatureValid(jwtToken)){
//                System.out.println("Invalid token");
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("Invalid JWT Token");
//                return; // Stop further processing
//            }

//            final String userEmail = jwtService.extractUsername(jwtToken);

//            try {
                // Extract and validate the token
                final String userEmail = jwtService.extractUsername(jwtToken);

                // Continue with authentication and user details...
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                System.out.println("userEmail " + userEmail);

                // caching user data
                // check if user if not authenticated -> authenticate here
                if(userEmail != null && authentication == null){
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                    if(jwtService.isTokenValid(jwtToken, userDetails)){
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        System.out.println("Invalid JWT for: " + request.getRequestURI());
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Token expired");
                        return;
                    }
                } else {
                    System.out.println("Missing JWT or uncached User for: " + request.getRequestURI());
                }

                filterChain.doFilter(request, response);

//            } catch (Exception e) {
//                System.out.println("jwt token error: " + e.getMessage());
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("jwt token error");
//            }


        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

}
