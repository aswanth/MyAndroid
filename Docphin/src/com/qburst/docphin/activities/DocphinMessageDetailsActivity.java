package com.qburst.docphin.activities;



import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qburst.docphin.R;
import com.qburst.docphin.activities.base.DocphinBaseActivity;
import com.qburst.docphin.apilisteners.DocphinMessageDetailsAPIListener;
import com.qburst.docphin.datamodels.DocphinMessageDetails;


public class DocphinMessageDetailsActivity extends DocphinBaseActivity implements DocphinMessageDetailsAPIListener{
	
	private View _messageDetailsLayout;
    private ViewGroup tabBar;
    private Button backButton;
    private TextView titleTextView, fromTextView, contentTextView, attachmentTextView;
    private int myMsgId;
    private ArrayList<DocphinMessageDetails> msgDetailsArray;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getBundleExtras();
        initViews();
        initClickListners();
        setViews();
        setLayout(_messageDetailsLayout, backButton);
       
    }
    private void getBundleExtras(){
	Bundle extras = getIntent().getExtras();
	myMsgId = extras.getInt("msgId");
	Log.d("msgId", myMsgId+"");

    }
    private void initViews()
    {
        _messageDetailsLayout = inflater.inflate(R.layout.docphin_messagedetails, null);

        tabBar = (ViewGroup) _messageDetailsLayout.findViewById(R.id.detailsLayout);
        backButton = (Button) tabBar.findViewById(R.id.backButton);
        backButton.setOnClickListener(new ClickListenerForScrolling(mainLayout,
                _menuLayout));
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        fromTextView = (TextView) findViewById(R.id.fromTextView);
        contentTextView = (TextView) findViewById(R.id.contentTextView);
        attachmentTextView = (TextView) findViewById(R.id.attachmentTextView);
    }

    private void initClickListners()
    {
        backButton.setOnClickListener(new ClickListenerForScrolling(mainLayout,
                _menuLayout));
    }

    private void setViews()
    {
    	//titleTextView.setText("hii");
    }
	public void messageDetailsFetchComplete(
			ArrayList<DocphinMessageDetails> messageDetails) {
		// TODO Auto-generated method stub
		
	}
	public void messagesDetailsFetchFailed() {
		// TODO Auto-generated method stub
		
	}
	
}
