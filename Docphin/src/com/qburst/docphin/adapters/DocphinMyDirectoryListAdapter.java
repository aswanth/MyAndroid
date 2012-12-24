package com.qburst.docphin.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qburst.docphin.R;

public class DocphinMyDirectoryListAdapter extends BaseAdapter
{
    private Activity _activity;
    private LayoutInflater _inflater;
    private View menuItemView;

    private TextView _name, _emailID, _phoneNO;
    private ImageView _sendEmailBtn, _callBtn;

    public DocphinMyDirectoryListAdapter(Activity activity)
    {

        _activity = activity;

        _inflater =
                (LayoutInflater) _activity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount()
    {
        // TODO Auto-generated method stub
        return 3;
    }

    public Object getItem(int arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int arg0)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int arg0, View arg1, ViewGroup arg2)
    {
        menuItemView =
                _inflater.inflate(R.layout.docphin_mydirectory_list_item, null);
        initViews(menuItemView);
        initClickListeners();
        return menuItemView;
    }

    private void initViews(View menuItemView)
    {
        _callBtn = (ImageView) menuItemView.findViewById(R.id.callBtn);
        _sendEmailBtn = (ImageView) menuItemView.findViewById(R.id.emailBtn);

    }

    private void initClickListeners()
    {
        _callBtn.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:123456789"));
                _activity.startActivity(callIntent);

            }
        });
        _sendEmailBtn.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {

            }
        });

    }

}
