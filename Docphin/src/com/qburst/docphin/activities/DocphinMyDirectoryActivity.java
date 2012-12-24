package com.qburst.docphin.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.qburst.docphin.R;
import com.qburst.docphin.activities.base.DocphinBaseActivity;
import com.qburst.docphin.adapters.DocphinMyDirectoryListAdapter;
import com.qburst.docphin.api.DocphinMyDirectoryAPI;
import com.qburst.docphin.apilisteners.DocphinMyDirectoryApiListener;
import com.qburst.docphin.datamodels.DocphinMyDirectoryModel;

public class DocphinMyDirectoryActivity extends DocphinBaseActivity implements
        DocphinMyDirectoryApiListener
{
    private View _myDirectoryLayout;

    private Button _paneButton;
    private ListView _myDirectryList;
    private ViewGroup tabBar;
    private DocphinMyDirectoryListAdapter _adapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initViews();
        initClickListners();
        setLayout(_myDirectoryLayout, _paneButton);
        DocphinMyDirectoryAPI api = new DocphinMyDirectoryAPI(this);
        api.getMyDirectory();

    }

    private void initViews()
    {
        _myDirectoryLayout =
                inflater.inflate(R.layout.docphin_mydirectory, null);

        tabBar = (ViewGroup) _myDirectoryLayout.findViewById(R.id.navheader);
        _paneButton = (Button) tabBar.findViewById(R.id.paneButton);
        _myDirectryList =
                (ListView) _myDirectoryLayout.findViewById(R.id.directoryList);
        _adapter = new DocphinMyDirectoryListAdapter(this);
        _myDirectryList.setAdapter(_adapter);

    }

    private void initClickListners()
    {
        _paneButton.setOnClickListener(new ClickListenerForScrolling(
                mainLayout, _menuLayout));
    }

    public void myDirectoryFetchComplete(
            ArrayList<DocphinMyDirectoryModel> messageList)
    {
        Toast.makeText(this, "succes", Toast.LENGTH_LONG).show();

    }

    public void myDirectoryFetchFailed()
    {
        Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();

    }

}