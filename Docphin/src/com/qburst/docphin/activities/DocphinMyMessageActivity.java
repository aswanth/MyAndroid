package com.qburst.docphin.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.qburst.docphin.R;
import com.qburst.docphin.activities.base.DocphinBaseActivity;
import com.qburst.docphin.adapters.DocphinMyMessageListAdapter;
import com.qburst.docphin.api.DocphinMyMessagesAPI;
import com.qburst.docphin.apilisteners.DocphinMyMessageAPIListener;
import com.qburst.docphin.datamodels.DocphinMessageModel;

public class DocphinMyMessageActivity extends DocphinBaseActivity implements
        DocphinMyMessageAPIListener, OnItemClickListener
{

    private View _myMessagesLayout;

    private Button paneButton;

    private ViewGroup tabBar;
    
    private ListView messageslist;

    
    private ArrayList<DocphinMessageModel> msgModelArray;

    private TextView title;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        setContentView(layoutResID);
        initViews();
        setUpViews();
        initClickListners();
        setLayout(_myMessagesLayout, paneButton);

        DocphinMyMessagesAPI myMessagesAPI = new DocphinMyMessagesAPI(this);
        myMessagesAPI.getMyMessages();

    }

    @Override
    protected void onResume()
    {

        super.onResume();
        setLayout(_myMessagesLayout, paneButton);
        initClickListners();
    }

    private void initViews()
    {

        _myMessagesLayout = inflater.inflate(R.layout.docphin_mymessages, null);

        tabBar = (ViewGroup) _myMessagesLayout.findViewById(R.id.paneLayout);
        paneButton = (Button) tabBar.findViewById(R.id.paneButton);
        
        messageslist = (ListView) _myMessagesLayout.findViewById(R.id.MyMessagesListView);

        messageslist.setOnItemClickListener(this);

        title = (TextView) _myMessagesLayout.findViewById(R.id.textviewTitle);
        

    }
    
    public void setUpViews() {
		title.setText("My Messages");
	}

    private void initClickListners()
    {
        paneButton.setOnClickListener(new ClickListenerForScrolling(mainLayout,
                _menuLayout));

    }
    
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
    	Intent intent = new Intent(DocphinMyMessageActivity.this, DocphinMessageDetailsActivity.class);
    	Log.i("message Id", "" + msgModelArray.get(position).getMessageID());
    	intent.putExtra("msgId", msgModelArray.get(position).getMessageID());
		startActivity(intent);
       
	}

    public void myMessagesFetchComplete(
            ArrayList<DocphinMessageModel> messageList)
 {
    	msgModelArray = messageList;
		// TODO Auto-generated method stub
		// for (int i = 0; i < messageList.size(); i++) {
		// Log.i("msg-" + i, messageList.get(i).getTitle());
		// }
    	
    
		DocphinMyMessageListAdapter messageAdapter = new DocphinMyMessageListAdapter(
				this, messageList);
		messageslist.setAdapter(messageAdapter);

	}

    public void myMessagesFetchFailed()
    {
        // TODO Auto-generated method stub

    }

	

}
