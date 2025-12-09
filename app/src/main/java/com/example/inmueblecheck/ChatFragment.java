package com.example.inmueblecheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatFragment extends Fragment {

    private ChatViewModel viewModel;
    private String inmuebleId;
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText etInput;
    private ImageButton btnSend;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            inmuebleId = getArguments().getString("inmuebleId");
        }

        recyclerView = view.findViewById(R.id.rvChatMessages);
        etInput = view.findViewById(R.id.etMessageInput);
        btnSend = view.findViewById(R.id.btnSend);
        toolbar = view.findViewById(R.id.toolbarChat);

        toolbar.setTitle("Chat de Inmuebles");
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        adapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.loadMessages(inmuebleId);

        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            adapter.setMessages(messages);
            if (!messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        });

        btnSend.setOnClickListener(v -> {
            String text = etInput.getText().toString();
            if (!text.trim().isEmpty()) {
                viewModel.sendMessage(inmuebleId, text);
                etInput.setText("");
            }
        });
    }
}