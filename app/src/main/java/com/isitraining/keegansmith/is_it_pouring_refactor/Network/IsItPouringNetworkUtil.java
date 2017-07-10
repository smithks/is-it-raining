package com.isitraining.keegansmith.is_it_pouring_refactor.Network;

/**
 * Util class to fetch an interface of the network api.
 * Created by keegansmith on 7/10/17.
 */

public class IsItPouringNetworkUtil {

    public static IsItPouringInterfaceAPI provideAPI(){
        return IsItPouringClientAPI.getClient().create(IsItPouringInterfaceAPI.class);
    }
}
