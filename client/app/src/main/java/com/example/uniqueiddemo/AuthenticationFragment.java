package com.example.uniqueiddemo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthenticationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthenticationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RadioGroup enc;
    private RadioButton type;


    public AuthenticationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AuthenticationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AuthenticationFragment newInstance(String param1, String param2) {
        AuthenticationFragment fragment = new AuthenticationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View authView = inflater.inflate(R.layout.fragment_authentication, container, false);
        Button auth = authView.findViewById(R.id.auth_button);
        enc = authView.findViewById(R.id.radio_group);
        enc.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Toast.makeText(authView.getContext(), "Encryption changed", Toast.LENGTH_SHORT).show();
            }
        });
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(),"Authenticated",Toast.LENGTH_SHORT).show();
            }
        });
        return authView;
    }

}