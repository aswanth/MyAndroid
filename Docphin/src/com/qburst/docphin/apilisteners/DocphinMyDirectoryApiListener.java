package com.qburst.docphin.apilisteners;

import java.util.ArrayList;

import com.qburst.docphin.datamodels.DocphinMyDirectoryModel;

public interface DocphinMyDirectoryApiListener
{
    public void myDirectoryFetchComplete(
            ArrayList<DocphinMyDirectoryModel> messageList);

    public void myDirectoryFetchFailed();
}
