package com.example.messenger.Fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.messenger.Adapter.UserAdapter;
import com.example.messenger.MainActivity;
import com.example.messenger.Model.ChatList;
import com.example.messenger.Model.Users;
import com.example.messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//This class displays recent chats with users
public class ChatsFragment extends Fragment {

    private UserAdapter userAdapter;
    private List<Users> mUsers = new ArrayList<>();
    private List<ChatList> chatLists = new ArrayList<>();
    private Users loggedUser;

    FirebaseUser firebaseUser;

    RecyclerView recyclerView;

    public ChatsFragment(Users loggedUser) {
        this.loggedUser = loggedUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recyclerView3);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("ChatFragment resumed");
        readChats();
    }

    private void readChats() {
        chatLists.clear();
        volleyGetChats(MainActivity.API_URL + String.format("/chatRoom/%s", firebaseUser.getUid()));
    }

    private void showChatList2() {
        mUsers.clear();
        volleyGetUsers(MainActivity.API_URL + "/api/user-all");
    }

    private void volleyGetChats(String getUrl) {

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, getUrl, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            final ChatList chatList = new ChatList(jsonObject.getString("recipientId"));

                            chatLists.add(chatList);
                        }
                    } catch (
                            JSONException e) {
                        e.printStackTrace();
                    }
                    showChatList2();
                },
                error -> error.printStackTrace());

        requestQueue.add(jsonArrayRequest);

    }

    private void volleyGetUsers(String getUrl) {

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, getUrl, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            final Users user = new Users(
                                    jsonObject.getString("id"),
                                    jsonObject.getString("name"),
                                    jsonObject.getString("imageURL"));

                            mUsers.add(user);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    filterUsers();
                    userAdapter = new UserAdapter(getContext(), mUsers);
                    recyclerView.setAdapter(userAdapter);
                },
                error -> error.printStackTrace());

        requestQueue.add(jsonArrayRequest);

    }

    private void filterUsers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            chatLists.forEach(chatList -> {
                mUsers.removeIf(s -> !s.getId().equals(chatList.getId()));
            });
        }

    }
}