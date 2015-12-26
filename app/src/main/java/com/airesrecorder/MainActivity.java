package com.airesrecorder;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    PlayerFragment playerFragment;
    RecorderFragment recorderFragment;

    CallBack callBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCallBack();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void initCallBack(){

        callBack = new CallBack() {
            @Override
            public void moveTo(int position) {
                mViewPager.setCurrentItem(1,true);
            }

            @Override
            public void refresh(int position) {

                if(position==0){
                    //recorderFragment.init();
                }else if(position==1){
                    playerFragment.init();
                }
            }
        };
    }

    public interface CallBack{

        public void moveTo(int position);
        public void refresh(int position);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0:{recorderFragment=RecorderFragment.newInstance(callBack);
                        return recorderFragment;
                }
                case 1:{playerFragment=  PlayerFragment.newInstance();
                        return playerFragment;
                }
                default: return null;
            }

        }

        @Override
        public int getCount() { return 2;}

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {

                case 0: return getResources().getString(R.string.section_record);
                case 1: return getResources().getString(R.string.section_player);
            }

            return null;
        }
    }

}
