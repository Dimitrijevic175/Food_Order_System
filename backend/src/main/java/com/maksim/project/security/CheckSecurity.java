package com.maksim.project.security;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckSecurity {

    String[] permissions() default {}; // Niz stringova za pojedinačne permisije
    String message() default "You don't have sufficient permissions"; // Poruka za grešku
}
