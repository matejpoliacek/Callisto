package com.chocolateam.galileospaceship;

import android.os.Bundle;
import android.view.View;

/**
 * Created by Matej on 03/02/2019.
 */

public class GraphicsTools {

    public static void hideShipDisabledWarning(View parentView, int warningImage, Bundle bundle) {
        boolean isNavDefault = true;

        if (bundle != null) {
            isNavDefault = bundle.getBoolean("isNavDefault", true);
        }

        if (!isNavDefault) {
            parentView.findViewById(warningImage).setVisibility(View.GONE);
        }
    }

    public static boolean checkIfGPSOnly(Bundle bundle) {
        boolean isGpsOnly = true;

        if (bundle != null) {
            isGpsOnly = bundle.getBoolean("isNavGpsOnly", true);
        }

        return isGpsOnly;
    }
}
