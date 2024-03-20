package com.example.myapplication.utilities;

import com.example.myapplication.R;
import com.example.myapplication.entities.Product;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.deviceapi.interfaces.ConnectionStatus;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class Scanner {

    private static Scanner scannerInstance;

    private final RFIDWithUHFUART RFIDReader;

    private Scanner() throws ConfigurationException {
        this.RFIDReader = RFIDWithUHFUART.getInstance();
    }

    public static Scanner getInstance() throws ConfigurationException {
        if (scannerInstance == null) {
            scannerInstance = new Scanner();
        }
        return scannerInstance;
    }

    public synchronized Completable initialize() throws Exception {
        return Completable.fromAction(() -> {
            if (RFIDReader.getConnectStatus() != ConnectionStatus.CONNECTED) {
                RFIDReader.init();
            }

            if (RFIDReader.getConnectStatus() != ConnectionStatus.CONNECTED) {
                throw new Exception("Failed to initialize RFID reader hardware.");
            }
        });
    }

    public Observable<Product> scan() {
        return Observable.create(emitter -> {
            while (!emitter.isDisposed()) {
                 UHFTAGInfo TAG = RFIDReader.inventorySingleTag();
                 if (TAG != null) {
                     emitter.onNext(new Product(TAG.getEPC()));
                 }
             }
        });
    }

    public synchronized Completable uninitialize() throws Exception {
        return Completable.fromAction(() -> {
            if (RFIDReader.getConnectStatus() != ConnectionStatus.DISCONNECTED) {
                RFIDReader.free();
            }

            if (RFIDReader.getConnectStatus() != ConnectionStatus.DISCONNECTED) {
                throw new Exception("Failed to unitialize RFID reader hardware.");
            }
        });
    }

    public synchronized void setPowerPercentage(int powerPercentage) {
        String version = RFIDReader.getVersion();
        if (version != null) {
            int power;
            if (version.contains("RLM")) {
                power = (int) Math.ceil(25*(powerPercentage/100.0));
            } else {
                power = (int) Math.ceil(30*(powerPercentage/100.0));
            }
            RFIDReader.setPower(power);
        }
    }

    public synchronized int getPowerPercentage() {
        String version = RFIDReader.getVersion();
        if (version != null) {
            int power = RFIDReader.getPower();
            if (version.contains("RLM")) {
                return (int) Math.ceil((power * 100.0)/25);
            } else {
                return (int) Math.ceil((power * 100.0)/30);
            }
        }

        return 0;
    }

    public String getVersion() {
        return RFIDReader.getVersion();
    }

    public ConnectionStatus getScannerStatus() {
        return RFIDReader.getConnectStatus();
    }
}