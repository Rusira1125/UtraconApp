package com.example.testingone.ui.gallery;

public class TaskModel {
    private String projectName;
    private String taskOne;
    private String date;
    private int rowIndex;

    public TaskModel(String projectName, String taskOne, String date, int rowIndex) {
        this.projectName = projectName;
        this.taskOne = taskOne;
        this.date = date;
        this.rowIndex = rowIndex;
    }

    public String getProjectName() { return projectName; }
    public String getTaskOne() { return taskOne; }
    public String getDate() { return date; }
    public int getRowIndex() { return rowIndex; }
}