/*
 * #%L
 * SlidingMenuDemo
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 Paul Grime
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.qburst.docphin.horizontalScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.qburst.docphin.R;
import com.qburst.docphin.activities.DocphinCalenderActivity;
import com.qburst.docphin.activities.DocphinCareerActivity;
import com.qburst.docphin.activities.DocphinCheckinActivity;
import com.qburst.docphin.activities.DocphinMessageDetailsActivity;
import com.qburst.docphin.activities.DocphinMyDirectoryActivity;
import com.qburst.docphin.activities.DocphinMyMessageActivity;
import com.qburst.docphin.activities.DocphinQuizActivity;
import com.qburst.docphin.adapters.DocphinMenuAdapter;
import com.qburst.docphin.datamodels.DocphinCalenderModel;

/**
 * Utility methods for Views.
 */
public class ViewUtils
{

    private ViewUtils()
    {
    }

    public static void setViewWidths(View view, View[] views)
    {
        int w = view.getWidth();
        int h = view.getHeight();
        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            v.layout((i + 1) * w, 0, (i + 2) * w, h);
            printView("view[" + i + "]", v);
        }
    }

    public static void printView(String msg, View v)
    {
        System.out.println(msg + "=" + v);
        if (null == v) {
            return;
        }
        System.out.print("[" + v.getLeft());
        System.out.print(", " + v.getTop());
        System.out.print(", w=" + v.getWidth());
        System.out.println(", h=" + v.getHeight() + "]");
        System.out.println("mw=" + v.getMeasuredWidth() + ", mh="
                + v.getMeasuredHeight());
        System.out.println("scroll [" + v.getScrollX() + "," + v.getScrollY()
                + "]");
    }

    public static void initListView(Context context, ListView listView,
            String prefix, int numItems, int layout)
    {
        // By using setAdpater method in listview we an add string array in list.
        String[] arr = new String[numItems];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = prefix + (i + 1);
        }
        listView.setAdapter(new ArrayAdapter<String>(context, layout, arr));
        listView.setOnItemClickListener(new OnItemClickListener()
        {

            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                Context context = view.getContext();
                String msg =
                        "item[" + position + "]="
                                + parent.getItemAtPosition(position);
                // Intent fbIntent = new Intent(context, FirstPage.class);
                // context.startActivity(fbIntent);
                System.out.println(msg);
            }
        });
    }

    public static View menuView(final Activity context)
    {

        LayoutInflater inflater = LayoutInflater.from(context);
        View menu = inflater.inflate(R.layout.docphin_menu, null);

        ListView listview = (ListView) menu.findViewById(R.id.menuItemList);
        DocphinMenuAdapter adapter = new DocphinMenuAdapter(context);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnItemClickListener()
        {

            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int position, long arg3)
            {
                switch (position) {
                case 0:
                    launchNewActivity(context, DocphinMyMessageActivity.class);
                    break;

                case 3:    
                    launchNewActivity(context, DocphinMyDirectoryActivity.class);

                    break;
                case 4:
                	 launchNewActivity(context, DocphinCalenderActivity.class);

                    break;
                case 5:                    
                    launchNewActivity(context, DocphinQuizActivity.class);
                    break;
                case 6:                    
                    launchNewActivity(context, DocphinCareerActivity.class);
                    break;
                case 7:                    
                    launchNewActivity(context, DocphinCheckinActivity.class);
                    break;
                default:

                }
            }
        });

        return menu;
    }
    
    public static void launchNewActivity(Activity cntxt, Class newClass) {
    	Intent navIntent =
                new Intent(cntxt, newClass);
    	cntxt.finish();
    	cntxt.startActivity(navIntent);
	}

}
