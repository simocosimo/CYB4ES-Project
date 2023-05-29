package com.example.uniqueiddemo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VerificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerificationFragment extends Fragment {

    private Thread netThread;
    private Button verifyBtn;
    private RecyclerView msgList;
    private EditText ip;
    private SymmVerifProcess symmVerifProcess;
    int code;
    public VerificationFragment() {
        // Required empty public constructor
    }

    public static VerificationFragment newInstance(String param1, String param2) {
        VerificationFragment fragment = new VerificationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View verifView = inflater.inflate(R.layout.fragment_verification, container, false);
        verifyBtn = verifView.findViewById(R.id.verify_btn);
        ip = verifView.findViewById(R.id.ip_addr_verif);
        msgList = verifView.findViewById(R.id.message_list);

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    symmVerifProcess = new SymmVerifProcess(verifView);
                    netThread = new Thread(symmVerifProcess);
                    netThread.start();
                    netThread.join();
                    code = symmVerifProcess.getCode();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (code == 0){
                    Toast.makeText(getContext(),"Please fill the ip field" + code, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getContext(),"Done: " + code, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return verifView;
    }
}