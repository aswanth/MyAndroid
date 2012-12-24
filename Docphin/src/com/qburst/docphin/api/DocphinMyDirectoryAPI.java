package com.qburst.docphin.api;

import org.w3c.dom.Element;

import android.util.Log;

import com.qburst.docphin.RequestModels.DocphinBaseRequestModel;
import com.qburst.docphin.Webservice.DocphinWebservice;
import com.qburst.docphin.apilisteners.DocphinMyDirectoryApiListener;
import com.qburst.docphin.app.DocphinApiConstants;
import com.qburst.docphin.responseparsers.DocphinMyDirectoryParser;

public class DocphinMyDirectoryAPI extends DocphinBaseAPI
{
    private DocphinWebservice _webservice;
    private DocphinMyDirectoryApiListener _listener;

    public DocphinMyDirectoryAPI(DocphinMyDirectoryApiListener _listener)
    {
        super();
        this._listener = _listener;
    }

    public void getMyDirectory()
    {
        _webservice = new DocphinWebservice(getRequestBody(), this);
        _webservice.execute();
    }

    @Override
    public void WebserviceFinishedWithResponse(String response, int statucode)
    {
        // TODO Auto-generated method stub
        super.WebserviceFinishedWithResponse(response, statucode);
        Log.i("MyMessagesResponse", response);
        DocphinMyDirectoryParser msgParser =
                new DocphinMyDirectoryParser(response);
        _listener.myDirectoryFetchComplete(msgParser.getdirectoryList());
    }

    @Override
    public void WebserviceFailedWithError(String error, int statucode)
    {
        // TODO Auto-generated method stub
        super.WebserviceFailedWithError(error, statucode);
        _listener.myDirectoryFetchFailed();
    }

    @Override
    public Element getXMLDocContent()
    {
        Element GetUserMessages =
                XMLRequestDoc
                        .createElement(DocphinApiConstants.DOCPHIN_MYDIRECTORY_URL);
        GetUserMessages.setAttribute("xmlns", DocphinApiConstants.DOCPHIN_URL);

        DocphinBaseRequestModel xmlModel =
                new DocphinBaseRequestModel(XMLRequestDoc,
                        GetUserMessages);

        return xmlModel.getRootElement();
    }

    @Override
    public String getSoapURL()
    {

        return super.getSoapURL() + DocphinApiConstants.DOCPHIN_MYDIRECTORY_URL;
    }

}
