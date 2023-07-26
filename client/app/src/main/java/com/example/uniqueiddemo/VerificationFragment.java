package com.example.uniqueiddemo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VerificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerificationFragment extends Fragment {

    private Thread netThread;
    private Button verifyBtn;
    private Switch verSwitch;
    private RecyclerView msgList;
    private SymmVerifProcess symmVerifProcess;
    private AsymmVerifProcess asymmVerifProcess;
    private ArrayList<VerifiedMessage> verified;
    private ArrayList<AsymmVerifiedMessage> asymmVerified;
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
        msgList = verifView.findViewById(R.id.message_list);
        verSwitch = verifView.findViewById(R.id.verification_switch);

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!verSwitch.isChecked()) {
                    // if switch is set to symmetric cryptography
                    try {
                        symmVerifProcess = new SymmVerifProcess(verifView);
                        netThread = new Thread(symmVerifProcess);
                        netThread.start();
                        netThread.join();
                        code = symmVerifProcess.getCode();
                        verified = symmVerifProcess.getVerified();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (code == 100){
                        Toast.makeText(getContext(),"Timeout", Toast.LENGTH_SHORT).show();
                    }
                    if (code == 0 ){
                        Toast.makeText(getContext(),"Please fill the ip field" + code, Toast.LENGTH_SHORT).show();
                    }else if (code == 201){
                        Toast.makeText(getContext(),"Done", Toast.LENGTH_SHORT).show();
                    }

                    RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(),verified);
                    msgList.setAdapter(adapter);
                    msgList.setLayoutManager(new LinearLayoutManager(getContext()));
                } else {
                    // if switch is set to asymmetric cryptography
                    try {
                        asymmVerifProcess = new AsymmVerifProcess(verifView);
                        netThread = new Thread(asymmVerifProcess);
                        netThread.start();
                        netThread.join();
                        code = asymmVerifProcess.getCode();
                        asymmVerified = asymmVerifProcess.getVerified();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (code == 100){
                        Toast.makeText(getContext(),"Timeout", Toast.LENGTH_SHORT).show();
                    }
                    if (code == 0 ){
                        Toast.makeText(getContext(),"Please fill the ip field" + code, Toast.LENGTH_SHORT).show();
                    }else if (code == 201){
                        Toast.makeText(getContext(),"Done", Toast.LENGTH_SHORT).show();
                    }

                    AsymmRecyclerViewAdapter adapter = new AsymmRecyclerViewAdapter(getContext(),asymmVerified);
                    msgList.setAdapter(adapter);
                    msgList.setLayoutManager(new LinearLayoutManager(getContext()));
                }
            }
        });
        return verifView;
    }
}