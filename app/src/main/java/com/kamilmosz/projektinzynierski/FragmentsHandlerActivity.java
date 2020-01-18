package com.kamilmosz.projektinzynierski;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.ViewGroup;

import com.kamilmosz.projektinzynierski.Fragments.BrowseSurgeriesFragment;
import com.kamilmosz.projektinzynierski.Fragments.EditInstrumentsFragment;
import com.kamilmosz.projektinzynierski.Fragments.ScannedInstrumentsFragment;
import com.kamilmosz.projektinzynierski.Models.NoSwipeViewPager;

import java.util.ArrayList;
import java.util.List;

public class FragmentsHandlerActivity extends AppCompatActivity {

    private NoSwipeViewPager viewPager;
    public Bundle bundle;
    public String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments_handler);

        retrieveBundle();
        init();
    }

    public void init() {
        viewPager = (NoSwipeViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setPagingEnabled(false);
    }

    public void retrieveBundle() {
        value = getIntent().getExtras().getString("my_key");
        bundle = new Bundle();
        bundle.putString("my_key_retrieved", value);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if(value.equals("edit")) {
            adapter.addFragment(new EditInstrumentsFragment(), "Edit instruments");
        }
        else if(value.equals("browse")) {
            adapter.addFragment(new BrowseSurgeriesFragment(), "Browse surgeries");
        }
        else {
            adapter.addFragment(new ScannedInstrumentsFragment(), "Scanned elements");
        }
        adapter.getItem(0).setArguments(bundle);
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            FragmentManager manager = ((Fragment) object).getFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();

            super.destroyItem(container, position, object);
        }
    }
}

