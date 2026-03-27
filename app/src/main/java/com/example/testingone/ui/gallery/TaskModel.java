package com.example.testingone.ui.gallery;

public class TaskModel {
    private String projectName;
    private String taskOne;
    private String date;

    public TaskModel(String projectName, String taskOne, String date) {
        this.projectName = projectName;
        this.taskOne = taskOne;
        this.date = date;
    }

    public String getProjectName() { return projectName; }
    public String getTaskOne() { return taskOne; }
    public String getDate() { return date; }
}