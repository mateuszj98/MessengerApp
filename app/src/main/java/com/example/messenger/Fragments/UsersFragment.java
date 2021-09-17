package com.example.messenger.Fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import com.example.messenger.Adapter.UserAdapter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.messenger.Adapter.UserAdapter;
import com.example.messenger.MainActivity;
import com.example.messenger.Model.Users;
import com.example.messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<Users> mUsers = new ArrayList<>();
    private Users loggedUser;
    private FirebaseUser firebaseUser;

    public UsersFragment() {
        // Required empty public constructor
    }

    public UsersFragment(Users loggedUser) {
        this.loggedUser = loggedUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        return view;
        //return inflater.inflate(R.layout.fragment_users,container,false);
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to  of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        System.out.println("UserFragment resumed");
        readUsers();
    }

    private void readUsers() {
        mUsers.clear();
        volleyGetUsers(MainActivity.API_URL + "/api/user-all");
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
                    //mUsers.remove(loggedUser);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        mUsers.removeIf(u -> u.getId().equals(firebaseUser.getUid()));
                    }
                    userAdapter = new UserAdapter(getContext(), mUsers);
                    recyclerView.setAdapter(userAdapter);
                },
                error -> error.printStackTrace());

        requestQueue.add(jsonArrayRequest);

    }
}