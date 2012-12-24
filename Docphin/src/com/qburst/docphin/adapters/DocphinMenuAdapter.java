package com.qburst.docphin.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qburst.docphin.R;

public class DocphinMenuAdapter extends BaseAdapter
{
    private Activity _activity;
    private LayoutInflater _inflater;
    private String[] menuItems;
    private View menuItemView;
    private TextView _menuName;

    public DocphinMenuAdapter(Activity activity)
    {
        _activity = activity;
        menuItems =
                _activity.getResources().getStringArray(
                        com.qburst.docphin.R.array.menu_items);
        _inflater =
                (LayoutInflater) _activity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount()
    {

        return menuItems.length;
    }

    public Object getItem(int arg0)
    {

        return null;
    }

    public long getItemId(int position)
    {

        return 0;
    }

    public View getView(int position, View arg1, ViewGroup arg2)
    {

        menuItemView = _inflater.inflate(R.layout.docphin_menu_list_item, null);

        _menuName = (TextView) menuItemView.findViewById(R.id.menuNameTxt);
        _menuName.setText(menuItems[position]);
        return menuItemView;
    }

}
