package com.example.testingone.ui.home;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.example.testingone.R;
import com.example.testingone.databinding.FragmentHomeBinding;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RequestQueue queue;
    private boolean isRequestInProgress = false; // ✅ prevent duplicate requests

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ✅ Initialize queue ONCE using application context
        if (queue == null) {
            queue = Volley.newRequestQueue(requireContext().getApplicationContext());
        }

        binding.add.setOnClickListener(view -> addItemToSheet());

        return root;
    }

    private void addItemToSheet() {

        // ✅ Block if request already running
        if (isRequestInProgress) {
            Toast.makeText(getContext(), "Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        String projectname = binding.projectName.getText().toString().trim();
        String taskone = binding.taskOne.getText().toString().trim();

        if (projectname.isEmpty() || taskone.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Lock the button
        isRequestInProgress = true;
        binding.add.setEnabled(false);

        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "Adding Item", "Please Wait...");

        String url = getString(R.string.deploymentID);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    dialog.dismiss();
                    isRequestInProgress = false;        // ✅ Unlock
                    binding.add.setEnabled(true);       // ✅ Re-enable button
                    Toast.makeText(getContext(), "Added successfully!", Toast.LENGTH_SHORT).show();
                    binding.projectName.setText("");
                    binding.taskOne.setText("");
                },
                error -> {
                    dialog.dismiss();
                    isRequestInProgress = false;        // ✅ Unlock on error too
                    binding.add.setEnabled(true);       // ✅ Re-enable button
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("projectName", projectname);
                params.put("taskOne", taskone);
                return params;
            }
        };

        // ✅ Tag the request so it can be cancelled if needed
        stringRequest.setTag("ADD_TASK");
        queue.add(stringRequest);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ✅ Cancel any pending requests when fragment stops
        if (queue != null) {
            queue.cancelAll("ADD_TASK");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}