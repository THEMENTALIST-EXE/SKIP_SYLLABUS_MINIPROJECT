package com.examheist.model;

/**
 * Represents a student attempting the Exam Heist.
 * 
 * Demonstrates:
 * - Classes and Objects
 * - Encapsulation (private fields with public getters)
 * - Constructor initialization
 */
public class Player {
    private String name;
    private String selectedSubject;

    // Constructor to initialize player details
    public Player(String name, String selectedSubject) {
        this.name = name;
        this.selectedSubject = selectedSubject;
    }

    public String getName() {
        return name;
    }

    public String getSelectedSubject() {
        return selectedSubject;
    }

    public void setSelectedSubject(String selectedSubject) {
        this.selectedSubject = selectedSubject;
    }

    @Override
    public String toString() {
        return "Agent " + name + " [" + selectedSubject + "]";
    }
}
