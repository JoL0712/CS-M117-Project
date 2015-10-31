package csm117.remotecommand;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import csm117.remotecommand.command.CommandsFragment;
import csm117.remotecommand.network.Connection;
import csm117.remotecommand.network.DiscoveryFragment;

public class MainActivity extends AppCompatActivity {
    public static String NO_CONNECTION = "Not connected to a device";
    ViewPager mViewPager;
    ViewPagerAdapter mViewPagerAdapter;
    Menu mMenu;
    DiscoveryFragment mDiscoveryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Connection.setMainActivity(this);

        setTitle(NO_CONNECTION);
        //TODO: Connection.connect() using last stored connection

        //add more fragment pages
        mDiscoveryFragment = new DiscoveryFragment();
        List<ViewPagerAdapter.FragmentHolder> fragmentHolders = new ArrayList<>();
        fragmentHolders.add(new ViewPagerAdapter.FragmentHolder("Commands", new CommandsFragment()));
        fragmentHolders.add(new ViewPagerAdapter.FragmentHolder("Devices", mDiscoveryFragment));

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentHolders);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);

        FixedTabViewer.init(this, mViewPager, mViewPagerAdapter, 17, 3, 40, 1, Color.parseColor("#00001f"), Color.TRANSPARENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, mMenu);
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
                break;
            case R.id.action_find_devices:
                mDiscoveryFragment.discover();
                break;
            case R.id.action_clear_devices:
                mDiscoveryFragment.clear();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            super.onBackPressed();
        else
            getSupportFragmentManager().popBackStack();
    }
}
