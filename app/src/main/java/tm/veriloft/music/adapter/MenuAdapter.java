/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import tm.veriloft.music.R;
import tm.veriloft.music.model.MenuActivity;
import tm.veriloft.music.model.MenuCategory;


public class MenuAdapter extends BaseAdapter {

    private Context mContext;
    private List<Object> menuItems;

    public MenuAdapter( Context _context, List<Object> _menuItems ) {
        mContext = _context;
        menuItems = _menuItems;
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem( int position ) {
        return menuItems.get(position);
    }

    @Override
    public long getItemId( int position ) {
        return position;
    }

    @Override
    public int getItemViewType( int position ) {
        return getItem(position) instanceof MenuActivity ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled( int position ) {
        return getItem(position) instanceof MenuActivity;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public View getView( int position, View _view, ViewGroup parent ) {
        View view = _view;
        Object menuItem = getItem(position);

        if (menuItem instanceof MenuCategory) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.view_navigation_drawer_menu_item, parent, false);
            }
            ((TextView) view).setText(((MenuCategory) menuItem).mTitle);
        } else {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.view_navigation_drawer_menu_item, parent, false);
            }

            TextView itemTitle = (TextView) view;
            itemTitle.setText(((MenuActivity) menuItem).mTitle);

            //Setting icon menu item
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                itemTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(((MenuActivity) menuItem).mIconResource, 0, 0, 0);
            } else {
                itemTitle.setCompoundDrawablesWithIntrinsicBounds(((MenuActivity) menuItem).mIconResource, 0, 0, 0);
            }
        }
        return view;
    }
}
