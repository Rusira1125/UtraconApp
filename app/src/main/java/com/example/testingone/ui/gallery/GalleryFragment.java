package com.example.testingone.ui.gallery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.testingone.R;
import com.example.testingone.databinding.FragmentGalleryBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private TaskAdapter adapter;
    private List<TaskModel> taskList;
    private RequestQueue queue;

    // ✅ Replace with your actual script URL
    // ✅ CORRECT - plain string, no context needed
    private final String SCRIPT_URL = "https://script.google.com/macros/s/AKfycby5MMfmVg1SJFR7Vn3NbhB77LMju-WKclyhj9l25F2z8lu_8SBj7DWLyuTyFQ0ge3A/exec";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ✅ Setup
        taskList = new ArrayList<>();
        queue = Volley.newRequestQueue(requireContext().getApplicationContext());

        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onEdit(TaskModel task, int position) {
                showEditDialog(task, position);
            }

            @Override
            public void onDelete(TaskModel task, int position) {
                showDeleteConfirmation(task, position);
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        fetchTasksFromSheet();

        return root;
    }

    private void fetchTasksFromSheet() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.textEmpty.setVisibility(View.GONE);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, SCRIPT_URL,
                response -> {
                    binding.progressBar.setVisibility(View.GONE);
                    try {
                        String cleanResponse = response.trim();

                        if (cleanResponse.startsWith("<")) {
                            Toast.makeText(getContext(), "Script error - redeploy", Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONArray jsonArray = new JSONArray(cleanResponse);
                        taskList.clear();

                        if (jsonArray.length() == 0) {
                            binding.textEmpty.setVisibility(View.VISIBLE);
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                String date = "";
                                try {
                                    date = obj.getString("date");
                                    if (date.contains("T")) {
                                        date = date.replace("T", " ").substring(0, 19);
                                    }
                                } catch (Exception ex) {
                                    date = "No date";
                                }

                                int rowIndex;
                                try {
                                    rowIndex = obj.getInt("rowIndex");
                                } catch (Exception ex) {
                                    rowIndex = i + 2;
                                }

                                taskList.add(new TaskModel(
                                        obj.getString("projectName"),
                                        obj.getString("taskOne"),
                                        date,
                                        rowIndex
                                ));
                            }
                            Collections.reverse(taskList);
                            adapter.notifyDataSetChanged();
                            binding.recyclerView.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("GALLERY_ERROR", "Error: " + e.getMessage());
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    binding.progressBar.setVisibility(View.GONE);
                    android.util.Log.e("GALLERY_ERROR", "Volley: " + error.toString());
                    Toast.makeText(getContext(), "Failed to load tasks", Toast.LENGTH_LONG).show();
                }
        );

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(stringRequest);
    }

    private void showEditDialog(TaskModel task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Task");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        EditText editProjectName = new EditText(getContext());
        editProjectName.setHint("Project Name");
        editProjectName.setText(task.getProjectName());
        layout.addView(editProjectName);

        EditText editTaskOne = new EditText(getContext());
        editTaskOne.setHint("Task");
        editTaskOne.setText(task.getTaskOne());
        layout.addView(editTaskOne);

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newProject = editProjectName.getText().toString().trim();
            String newTask = editTaskOne.getText().toString().trim();

            if (newProject.isEmpty() || newTask.isEmpty()) {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            updateTask(task, newProject, newTask, position);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteConfirmation(TaskModel task, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete \"" + task.getProjectName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTask(task, position))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateTask(TaskModel task, String newProject, String newTask, int position) {
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "Updating", "Please Wait...");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SCRIPT_URL,
                response -> {
                    dialog.dismiss();
                    taskList.set(position, new TaskModel(newProject, newTask, task.getDate(), task.getRowIndex()));
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "update");
                params.put("rowIndex", String.valueOf(task.getRowIndex()));
                params.put("projectName", newProject);
                params.put("taskOne", newTask);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void deleteTask(TaskModel task, int position) {
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "Deleting", "Please Wait...");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SCRIPT_URL,
                response -> {
                    dialog.dismiss();
                    taskList.remove(position);
                    adapter.notifyItemRemoved(position);
                    if (taskList.isEmpty()) {
                        binding.textEmpty.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "delete");
                params.put("rowIndex", String.valueOf(task.getRowIndex()));
                return params;
            }
        };

        queue.add(stringRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}