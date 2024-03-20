package com.example.myapplication.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.id.AssetId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Single;

public class ThingsBoard {
    RestClient client;

    private final String url;
    private final String username;
    private final String password;

    private PageData<Asset> assets;

    private final List<String> STORAGE_TYPES = Arrays.asList(new String[] {"Truck","Storage"});

    public ThingsBoard(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        username = sharedPreferences.getString("thingsboard-tenant-username", "");
        password = sharedPreferences.getString("thingsboard-tenant-password", "");
        url = sharedPreferences.getString("thingsboard-domain", "");
        client = new RestClient(url);
        client.login(username,password);
    }

    public void connection_close(){
        client.logout();
        client.close();
    }

    public void login(){
        client.login(username,password);
    }

    public PageData<Asset> getTenantAssetsByType(String assetType) {
        PageLink pageLink = new PageLink(1000);
        assets = client.getTenantAssets(pageLink, assetType);
        return assets;
    }

    public ArrayList<String> getAssetsNames(PageData <Asset> asset) {
        ArrayList<String> Names = new ArrayList<>();
        for(int i=0;i <asset.getTotalElements();i++) {
            Names.add(asset.getData().get(i).getName());
        }
        return Names;
    }

    public List<Asset> getTenantAssets() {
        List<String> assetTypes = client.getAssetTypes().stream().map(entitySubtype -> entitySubtype.getType()).collect( Collectors.toList());
        List<Asset> assets = new ArrayList<>();
        for (String type : assetTypes) {
            assets.addAll(this.getTenantAssetsByType(type).getData());
        }

        return assets;
    }

    public AssetId QueryIDbyName(String RFID){
        AssetId Id = client.findAsset(RFID).get().getId();
        return Id;
    }

    public String QueryTypebyName(String RFID){
        return client.findAsset(RFID).get().getType();
    }

    public void ChangeRelationship(String FromName, String ToLocation){
        EntityId FromID = QueryIDbyName(FromName);
        EntityId ToLocationID = QueryIDbyName(ToLocation);
        String FromAssetType = QueryTypebyName(FromName);
        String ToAssetType = QueryTypebyName(ToLocation);
        List<String> AcceptedTypes = client.getAssetTypes().stream().map(entitySubtype -> entitySubtype.getType()).collect( Collectors.toList());
        if(AcceptedTypes.contains(FromAssetType)&&AcceptedTypes.contains(ToAssetType)){
            client.deleteRelations(FromID);
            EntityRelation newRel = new EntityRelation(ToLocationID, FromID,"Contains");
            client.saveRelation(newRel);
        }
    }

    public ArrayList<String> getItemsByTypes(List<String> types,ThingsBoard brd){
        ArrayList<String > items = new ArrayList<>();

        brd.login();
        for(String type: types) {
            items.addAll(brd.getAssetsNames(brd.getTenantAssetsByType(type)));
        }
        brd.connection_close();

        return items;
    }

    public ArrayList<String> getLocations() {
        ArrayList<String> locations = new ArrayList<>();
        for(String type: STORAGE_TYPES) {
            locations.addAll(this.getAssetsNames(this.getTenantAssetsByType(type)));
        }

        return locations;
    }

    public List<String> getProductTypes() {
        return client.getAssetTypes().stream()
                .map(entitySubtype -> entitySubtype.getType())
                .filter(type -> {
                    return !STORAGE_TYPES.contains(type);
                })
                .collect( Collectors.toList());
    }

    public String toString(){
        return client.getUser().toString();
    }
}
