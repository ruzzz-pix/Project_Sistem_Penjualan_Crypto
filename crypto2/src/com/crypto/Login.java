package com.crypto;
public interface Login {
    void signUp(String username, String password);  // otomatis public abstract

    boolean signIn(String username, String password);  // otomatis public abstract
}
