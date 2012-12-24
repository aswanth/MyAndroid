package com.qburst.docphin.responseparsers;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.qburst.docphin.datamodels.DocphinMyDirectoryModel;
import com.qburst.docphin.utilities.DocphinUtilities;

public class DocphinMyDirectoryParser
{
    String xml;
    Document document;

    public DocphinMyDirectoryParser(String xml)
    {
        super();
        this.xml = xml;
        document = DocphinUtilities.getXMLObj(xml);
    }

    public ArrayList<DocphinMyDirectoryModel> getdirectoryList()
    {
        Node dirNode;
        NodeList dirNodeChildren;
        DocphinMyDirectoryModel dirModel;

        NodeList directoryNodes =
                document.getElementsByTagName("SimpleConnectUser");
        ArrayList<DocphinMyDirectoryModel> directoryList =
                new ArrayList<DocphinMyDirectoryModel>();

        for (int i = 0; i < directoryNodes.getLength(); i++) {

            dirNode = directoryNodes.item(i);
            dirNodeChildren = dirNode.getChildNodes();
            dirModel = new DocphinMyDirectoryModel();

            for (int j = 0; j < dirNodeChildren.getLength(); j++) {

                Node node = dirNodeChildren.item(j);
                if (node.getNodeName().equals("FullName")) {
                    dirModel.setFullName(node.getTextContent());
                } else if (node.getNodeName().equals("Email")) {
                    dirModel.setEmail(node.getTextContent());
                } else if (node.getNodeName().equals("Phones")) {
                    Log.e("Phones", node.getTextContent());
                } else if (node.getNodeName().equals("CurrentRotation")) {
                    dirModel.setCurrentRotation(node.getTextContent());
                }
                directoryList.add(dirModel);
            }

        }
        return directoryList;
    }

}