package csm117.remotecommand;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import csm117.remotecommand.command.CommandsFragment;
import csm117.remotecommand.network.DiscoveryFragment;

public class MainActivity extends AppCompatActivity {
    ViewPager mViewPager;
    ViewPagerAdapter mViewPagerAdapter;
    Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //add more fragment pages
        List<ViewPagerAdapter.FragmentHolder> fragmentHolders = new ArrayList<>();
        fragmentHolders.add(new ViewPagerAdapter.FragmentHolder("Commands", new CommandsFragment()));
        fragmentHolders.add(new ViewPagerAdapter.FragmentHolder("Devices", new DiscoveryFragment()));

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentHolders);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);

        FixedTabViewer.init(this, mViewPager, mViewPagerAdapter, 17, 3, 40, 1, Color.parseColor("#00001f"), Color.TRANSPARENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                //TODO
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            super.onBackPressed();
        else
            getSupportFragmentManager().popBackStack();
    }
}
