package com.example.testingone.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.testingone.R;
import com.example.testingone.databinding.FragmentGalleryBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private TaskAdapter adapter;
    private List<TaskModel> taskList;
    private RequestQueue queue;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ✅ Setup RecyclerView
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        queue = Volley.newRequestQueue(requireContext().getApplicationContext());

        // ✅ Fetch data when fragment opens
        fetchTasksFromSheet();

        return root;
    }

    private void fetchTasksFromSheet() {
        // ✅ Show loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.textEmpty.setVisibility(View.GONE);

        // ✅ Same URL as your POST but uses GET
        String url = getString(R.string.deploymentID);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    binding.progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        taskList.clear();

                        if (jsonArray.length() == 0) {
                            // ✅ Show empty message
                            binding.textEmpty.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.GONE);
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                String projectName = obj.getString("projectName");
                                String taskOne = obj.getString("taskOne");
                                String date = obj.getString("date");
                                taskList.add(new TaskModel(projectName, taskOne, date));
                            }
                            // ✅ Reverse so newest shows first
                            java.util.Collections.reverse(taskList);
                            adapter.notifyDataSetChanged();
                            binding.recyclerView.setVisibility(View.VISIBLE);
                        }

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error reading data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(stringRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}