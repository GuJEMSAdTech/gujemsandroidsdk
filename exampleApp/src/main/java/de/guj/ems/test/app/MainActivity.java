package de.guj.ems.test.app;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;

import de.guj.ems.mobile.sdk.util.SdkUtil;
import de.guj.ems.mobile.sdk.util.SourcePointCMP;
import de.guj.ems.mobile.sdk.util.YieldLab;
import de.guj.ems.test.app.view.FixedTabsPagerAdapter;
import de.guj.ems.test.app.view.SlidingTabLayout;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init util class
        util.init(this);
        SdkUtil.setContext(getApplicationContext());
        HashMap<String, String> ylMap = new HashMap<String, String>();
        ylMap.put("mpa", "7509781");
        ylMap.put("msa", "7509784");
        ylMap.put("mca", "7509790");
        ylMap.put("mda", "7509787");
        YieldLab.init(ylMap, 4);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
            PagerAdapter pagerAdapter = new FixedTabsPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(pagerAdapter);

            SlidingTabLayout tabLayout = (SlidingTabLayout) findViewById(R.id.tab_layout);
            tabLayout.setViewPager(viewPager);
        }

        SourcePointCMP cmp = new SourcePointCMP(
            this,
            PreferenceManager.getDefaultSharedPreferences(this),
            true,
            false);
        cmp.initConsent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int height = getWindow().getDecorView().getHeight();
        int width = getWindow().getDecorView().getWidth();
        menu.add("Screen Width: " + width);
        menu.add("Screen Height: " + height);

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
