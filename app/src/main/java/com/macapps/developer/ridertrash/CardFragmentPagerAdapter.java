package com.macapps.developer.ridertrash;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.CardView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class CardFragmentPagerAdapter extends FragmentStatePagerAdapter implements CardAdapter {
    private List<CardFragment> fragments;
    private float baseElevation;
    private ArrayList<Bus> buses;



    public CardFragmentPagerAdapter(FragmentManager fm, float baseElevation,ArrayList<Bus>buses) {

        super(fm);
        fragments = new ArrayList<>();
        this.baseElevation = baseElevation;
        this.buses=buses;
    }

    @Override
    public float getBaseElevation() {
        return baseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return fragments.get(position).getCardView();
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return CardFragment.getInstance(position,buses.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Bundle bundle=new Bundle();
        bundle.putParcelable("bus",buses.get(position));

        Object fragment = super.instantiateItem(container, position);
        CardFragment cardFragment=new CardFragment();
        cardFragment.setArguments(bundle);

        fragments.set(position, cardFragment);
        return fragment;
    }

    public void addCardFragment(CardFragment fragment) {
        fragments.add(fragment);

    }

}
