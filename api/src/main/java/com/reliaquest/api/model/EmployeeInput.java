package com.reliaquest.api.model;

public class EmployeeInput {
    private String name;
    private int salary;
    private int age;
    private String title;

    public EmployeeInput() {
    }

    public EmployeeInput(String name, int salary, int age, String title) {
        this.name = name;
        this.salary = salary;
        this.age = age;
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
