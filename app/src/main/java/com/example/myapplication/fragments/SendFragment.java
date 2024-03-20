package com.example.myapplication.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.ListPreference;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.entities.Product;
import com.example.myapplication.utilities.ThingsBoard;

import java.util.ArrayList;

public class SendFragment extends Fragment {
    public static SendFragment newInstance() {
        SendFragment fragment = new SendFragment();
        return fragment;
    }

    public SendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Product[] checkedProducts = SendFragmentArgs.fromBundle(getArguments()).getCheckedProducts();
        String location = SendFragmentArgs.fromBundle(getArguments()).getLocation();

        ArrayList<String> asset = new ArrayList<>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ThingsBoard brd =  new ThingsBoard(getContext());
                    brd.login();
                    for(Product prod :checkedProducts) {
                        brd.ChangeRelationship(prod.toString(),location);
                    }
                    brd.connection_close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Toast toast = Toast.makeText(getContext(), "Products moved successfully to " + location, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,300);
        toast.show();

        NavHostFragment.findNavController(SendFragment.this)
                .navigate(R.id.action_sendFragment_to_homeFragment);
    }
}