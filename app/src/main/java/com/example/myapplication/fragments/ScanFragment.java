package com.example.myapplication.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.myapplication.R;
import com.example.myapplication.entities.Product;
import com.example.myapplication.utilities.Scanner;
import com.example.myapplication.utilities.ThingsBoard;
import com.rscja.deviceapi.exception.ConfigurationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanFragment extends Fragment {
    private Disposable disposable;
    private Set<Product> productSet;
    private List<Product> productList;
    public ScanFragment() throws Exception {
        // Required empty public constructor
    }

    public static ScanFragment newInstance() throws Exception {
        ScanFragment fragment = new ScanFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View scanFragmentView = inflater.inflate(R.layout.fragment_scan, container, false);

        productList = new ArrayList<>();
        productSet = new HashSet<>();

        PopupWindow popupWindow = getScanningPopUpWindow();

        ListView listView = (ListView) scanFragmentView.findViewById(R.id.scan_fg_items_list_view);
        final ArrayAdapter<Product> productAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, productList);
        listView.setAdapter(productAdapter);

        Button continueButton = (Button) scanFragmentView.findViewById(R.id.scan_fg_continue_bt);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                ScanFragmentDirections.ActionScanFragmentToCheckFragment scanFragmentToCheckFragment =
                        ScanFragmentDirections.actionScanFragmentToCheckFragment(productSet.toArray(new Product[productSet.size()]));
                NavHostFragment.findNavController(ScanFragment.this)
                        .navigate(scanFragmentToCheckFragment);
            }
        });

        Button scanButton = (Button) scanFragmentView.findViewById(R.id.scan_fg_scan_bt);
        scanButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_play_arrow_24, 0);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (scanButton.getText().toString()) {
                    case "Scan":
                        scanButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_stop_24, 0);
                        scanButton.setText("Stop");
                        continueButton.setVisibility(View.INVISIBLE);

                        popupWindow.showAtLocation(scanFragmentView, Gravity.CENTER, 0, 0);

                        try {
                            Scanner.getInstance().scan()
                                    .takeWhile(product -> scanButton.getText().equals("Stop"))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<Product>() {

                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {
                                            disposable = d;
                                        }

                                        @Override
                                        public void onNext(@NonNull Product product) {
                                            if (productSet.add(product)) {
                                                productAdapter.add(product);
                                                productAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {
                                        }

                                        @Override
                                        public void onComplete() {
                                        }
                                    });
                        } catch (ConfigurationException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Stop":
                        scanButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_play_arrow_24, 0);
                        scanButton.setText("Scan");
                        continueButton.setVisibility(View.VISIBLE);

                        popupWindow.dismiss();

                        if (!disposable.isDisposed()) {
                            disposable.dispose();
                        }

                        break;
                }
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                popupWindow.dismiss();
                NavHostFragment.findNavController(ScanFragment.this)
                        .navigate(R.id.action_scanFragment_to_homeFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return scanFragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == 139 || keyCode == 280 || keyCode == 293)) {
                    Button scanButton = (Button) view.findViewById(R.id.scan_fg_scan_bt);
                    scanButton.performClick();
                }
                return false;
            }
        });
    }

    public PopupWindow getScanningPopUpWindow() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View popupView = inflater.inflate(R.layout.popupwindow_loading, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height);
        popupWindow.setFocusable(false);
        popupWindow.setTouchable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupWindow.setTouchModal(false);
        }

        return popupWindow;
    }

    @Override
    public void onResume() {
        super.onResume();
        productList = new ArrayList<>();
        productSet = new HashSet<>();
    }
}