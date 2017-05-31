package com.macapps.developer.ridertrash;

import android.support.v7.widget.CardView;

/**
 * Created by Developer on 30/5/2017.
 */

public interface CardAdapter {

    int MAX_ELEVATION_FACTOR = 8;

    float getBaseElevation();

    CardView getCardViewAt(int position);

    int getCount();
}
