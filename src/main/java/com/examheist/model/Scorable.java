package com.examheist.model;

/**
 * Interface defining the capability to yield a numeric game score.
 * 
 * Demonstrates:
 * - Interface definition
 * - Decoupling contracts from concrete implementations
 * - Polymorphism through interface references
 */
public interface Scorable {
    int getScore();
}
