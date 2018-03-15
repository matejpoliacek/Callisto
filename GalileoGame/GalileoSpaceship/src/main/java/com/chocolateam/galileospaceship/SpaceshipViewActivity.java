package com.chocolateam.galileospaceship;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.example.lionelgarcia.galileospaceship.R;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class SpaceshipViewActivity extends AppCompatActivity {

    static final int NUM_PANELS = 3;
    SpacecraftPagerAdapter mAdapter;
    ViewPager mPager;

    static ListViewFragment mListViewFragment;
    static SkyViewFragment mSkyViewFragment;
    static RadarViewFragment mRadarViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.spacecraft_pager);

        mListViewFragment = new ListViewFragment();
        mSkyViewFragment = new SkyViewFragment();
        mRadarViewFragment = new RadarViewFragment();

        mAdapter = new SpacecraftPagerAdapter(getSupportFragmentManager());

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
//        mPager.setOffscreenPageLimit(2);

        mPager.setCurrentItem(1);
    }

    public static class SpacecraftPagerAdapter extends FragmentPagerAdapter {
        public SpacecraftPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_PANELS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mListViewFragment;
                case 1:
                    return mSkyViewFragment;
                case 2:
                    return mRadarViewFragment;
                default:
                    return new Fragment();
            }

        }
    }

}
