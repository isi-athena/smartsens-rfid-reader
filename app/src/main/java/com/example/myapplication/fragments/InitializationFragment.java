package com.example.myapplication.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.utilities.Scanner;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InitializationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitializationFragment extends Fragment {

    public InitializationFragment() {
        // Required empty public constructor
    }

    public static InitializationFragment newInstance() {
        InitializationFragment fragment = new InitializationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_initialization, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            Scanner.getInstance().initialize()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            NavHostFragment.findNavController(InitializationFragment.this)
                                    .navigate(R.id.action_initializationFragment_to_homeFragment);
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                            onInitializationError(view.getContext());
                        }
                    });
        } catch (Exception e) {
            onInitializationError(view.getContext());
        }
    }

    private void onInitializationError(Context context) {
        Toast.makeText(context, "Failed to initialize device's hardware.", Toast.LENGTH_SHORT).show();
        getActivity().finish();
    }
}