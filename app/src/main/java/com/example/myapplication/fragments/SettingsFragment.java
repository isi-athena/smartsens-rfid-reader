package com.example.myapplication.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SeekBarPreference;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.entities.Product;
import com.example.myapplication.utilities.Scanner;
import com.example.myapplication.utilities.ThingsBoard;
import com.rscja.deviceapi.exception.ConfigurationException;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingsFragment extends PreferenceFragmentCompat {
    private ThingsBoard brd;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) {
            public PreferenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                PreferenceViewHolder holder = super.onCreateViewHolder(parent, viewType);
                View customLayout = holder.itemView;
                return holder;
            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setPowerPreferenceData();

        ListPreference listPreference = (ListPreference) findPreference("loc_list");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    brd =  new ThingsBoard(getContext());
                    brd.login();
                    setListPreferenceData(listPreference);
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

        EditTextPreference passwordPreference = (EditTextPreference) findPreference("thingsboard-tenant-password");
        if (passwordPreference!= null) {
            passwordPreference.setSummaryProvider(new Preference.SummaryProvider() {
                @Override
                public CharSequence provideSummary(Preference preference) {
                    return Stream.generate(() -> "●")
                            .limit(passwordPreference.getText().length())
                            .collect(Collectors.joining());
                }
            });
            passwordPreference.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@androidx.annotation.NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                    });
        }
    }

    protected void setListPreferenceData(ListPreference lp) {
        ArrayList<String> locations = brd.getLocations();

        CharSequence[] entries  = locations.toArray(new CharSequence[locations.size()]);
        CharSequence[] entryValues  = locations.toArray(new CharSequence[locations.size()]);
        lp.setEntries(entries);
        lp.setDefaultValue("1");
        lp.setEntryValues(entryValues);
    }

    protected void setPowerPreferenceData() {
        Scanner scanner;

        try {
            scanner = Scanner.getInstance();
        } catch (ConfigurationException e) {
            return;
        }

        SeekBarPreference seekBarPreference = (SeekBarPreference) findPreference("scanner_power");

        seekBarPreference.setOnPreferenceChangeListener((preference, powerPercentage) -> {
            scanner.setPowerPercentage((int) powerPercentage);
            return true;
        });

        seekBarPreference.setDefaultValue(scanner.getPowerPercentage());
    }
}