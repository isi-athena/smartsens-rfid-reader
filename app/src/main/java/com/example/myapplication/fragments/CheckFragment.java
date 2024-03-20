package com.example.myapplication.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.example.myapplication.R;
import com.example.myapplication.entities.Product;
import com.example.myapplication.utilities.ThingsBoard;
import com.google.android.material.textfield.TextInputLayout;

import org.thingsboard.server.common.data.asset.Asset;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckFragment extends Fragment {
    static volatile ThingsBoard  brd;
    static volatile ArrayList<String> productsOnBoard;
    static volatile ArrayList<Product> productsOnBoard1;
    static volatile ArrayList<String> locations;
    static volatile ArrayList<Asset> assets;

    public CheckFragment() { }

    public static CheckFragment newInstance() {
        CheckFragment fragment = new CheckFragment();
        return fragment;
    }

    private List<Product> productList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View checkFragmentView = inflater.inflate(R.layout.fragment_check, container, false);

        this.productList = Arrays.asList(CheckFragmentArgs.fromBundle(getArguments()).getProducts());
        ListView listView = (ListView) checkFragmentView.findViewById(R.id.check_fg_items_list_view);

        productsOnBoard = new ArrayList<>();
        locations = new ArrayList<>();
        assets = new ArrayList<>();
        productsOnBoard1 = new ArrayList<>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                brd =  new ThingsBoard(getContext());
                productsOnBoard.addAll(brd.getItemsByTypes(brd.getProductTypes(),brd));
                locations.addAll(brd.getLocations());
                assets.addAll(brd.getTenantAssets());
            }
        });

        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                NavHostFragment.findNavController(CheckFragment.this)
                        .navigate(R.id.action_checkFragment_to_homeFragment);
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("LOC", locations.toString());

        ArrayList<String> temp = new ArrayList<>();
        for(Product pr : productList){
            if(productsOnBoard.contains(pr.toString())){
                productsOnBoard1.add(pr);
                for (Asset asset : assets) {
                    if (pr.getId().equals(asset.getName())) {
                        temp.add(pr.getId().substring(15) + " " + asset.getLabel());
                    }
                }
            }
        }

        final ArrayAdapter<String> productAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, temp);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(productAdapter);

        for (int i=0; i < productAdapter.getCount(); i++) {
            listView.setItemChecked(i, true);
        }

        ArrayAdapter<String> locationAdapter = new ArrayAdapter(requireContext(), androidx.preference.R.layout.support_simple_spinner_dropdown_item, locations);

        TextInputLayout textInputLayout = checkFragmentView.findViewById(R.id.menu);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)textInputLayout.getEditText();
        autoCompleteTextView.setAdapter(locationAdapter);
        autoCompleteTextView.setText(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("loc_list", ""), false);

        Button sendButton = (Button) checkFragmentView.findViewById(R.id.check_fg_continue_bt);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Product[] checkedProducts = new Product[listView.getCheckedItemCount()];
                SparseBooleanArray checked = listView.getCheckedItemPositions();

                int j=0;
                for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                    if (checked.get(i)) {
                        checkedProducts[j++] = productsOnBoard1.get(i);
                    }
                }

                String location = textInputLayout.getEditText().getText().toString();

                CheckFragmentDirections.ActionCheckFragmentToSendFragment checkFragmentToSendFragment =
                        CheckFragmentDirections.actionCheckFragmentToSendFragment(checkedProducts, location);
                NavHostFragment.findNavController(CheckFragment.this)
                        .navigate(checkFragmentToSendFragment);

            }
        });

        return checkFragmentView;
    }
}