package csm117.remotecommand;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<FragmentHolder> mFragments;

    public static class FragmentHolder {
        private String mTitle;
        private Fragment mFragment;

        public FragmentHolder(String title, Fragment fragment) {
            mTitle = title;
            mFragment = fragment;
        }
        public String getTitle() { return mTitle; }
        public Fragment getFragment() { return mFragment; }
    }

    public ViewPagerAdapter(FragmentManager fm, List<FragmentHolder> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position).getFragment();
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getTitle();
    }
}
