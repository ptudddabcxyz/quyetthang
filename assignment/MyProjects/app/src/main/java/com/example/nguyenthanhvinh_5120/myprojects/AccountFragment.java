package com.example.nguyenthanhvinh_5120.myprojects;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        Bundle bundle = getArguments();
        String username = bundle.getString("username");
        String email = bundle.getString("email");

        TextView tvUsername = (TextView) v.findViewById(R.id.tvUsername);
        TextView tvEmail = (TextView) v.findViewById(R.id.tvEmail);

        tvUsername.setText("Username: "+username);
        tvEmail.setText("Email: "+email);

        return v;
    }

}
