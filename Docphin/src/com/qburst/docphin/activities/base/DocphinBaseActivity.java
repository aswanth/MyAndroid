package com.qburst.docphin.activities.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.SlidingDrawer;

import com.qburst.docphin.R;
import com.qburst.docphin.horizontalScrollView.DocphinHorizontalScrollView;
import com.qburst.docphin.horizontalScrollView.DocphinHorizontalScrollView.SizeCallback;
import com.qburst.docphin.horizontalScrollView.ViewUtils;

public class DocphinBaseActivity extends Activity {

    protected DocphinHorizontalScrollView mainLayout;
    protected LayoutInflater inflater;
    protected View _menuLayout;
    private View[] childLayout;
    private int scrollToViewIdx = 1;
    private ProgressDialog ApiProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (inflater == null) {

            inflater = LayoutInflater.from(this);
            mainLayout =
                    (DocphinHorizontalScrollView) inflater.inflate(
                            R.layout.docphin_home, null);
            _menuLayout = ViewUtils.menuView(this);
        }
    }
    
    public void showProgressDialog(Context context) {
        ApiProgressDialog = new ProgressDialog(context);
        ApiProgressDialog.setMessage(getString(R.string.wait));
        ApiProgressDialog.setCancelable(false);
        ApiProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ApiProgressDialog.show();

    }

    public void dismissProgressDialog() {
        if (ApiProgressDialog != null) {
            try {
                ApiProgressDialog.dismiss();
                Log.d("qblog", "dialog dismissed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setLayout(View appLayout, Button paneButton) {
        childLayout = new View[] { _menuLayout, appLayout };
        mainLayout.initViews(childLayout, scrollToViewIdx,
                new SizeCallbackForMenu(paneButton));
        setContentView(mainLayout);
    }

    public static class SizeCallbackForMenu implements SizeCallback {
        int btnWidth;
        View btnSlide;
        int offset_width = 0;

        public SizeCallbackForMenu(View btnSlide) {
            super();
            this.btnSlide = btnSlide;
        }

        public void onGlobalLayout() {
            btnWidth = btnSlide.getMeasuredWidth() + 2 * btnSlide.getLeft() + offset_width;
            System.out.println("btnWidth=" + btnWidth);
        }

        public void getViewSize(int idx, int w, int h, int[] dims) {
            dims[0] = w;
            dims[1] = h;
            final int menuIdx = 0;
            if (idx == menuIdx) {
                dims[0] = w - btnWidth;
            }
        }

		@Override
		public void setOffsetWidth(int width) {
			// TODO Auto-generated method stub
			offset_width += width;
		}
		@Override
		public void initOffsetWidth() {
			// TODO Auto-generated method stub
			offset_width = 0;
		}

		@Override
		public int getOffsetWidth() {
			// TODO Auto-generated method stub
			return offset_width;
		}

		@Override
		public Button getSlideButton() {
			// TODO Auto-generated method stub
			return (Button)btnSlide;
		}
    }

    public static class ClickListenerForScrolling implements OnClickListener {
        HorizontalScrollView scrollView;
        View menu;
        /**
         * Menu must NOT be out/shown to start with.
         */
        boolean menuOut = false;

        public ClickListenerForScrolling(HorizontalScrollView scrollView,
                View menu){
            super();
            this.scrollView = scrollView;
            this.menu = menu;
        }

        public void onClick(View v) {
            Context context = menu.getContext();

            int menuWidth = menu.getMeasuredWidth();

            // Ensure menu is visible
            menu.setVisibility(View.VISIBLE);

            if (!menuOut) {
                // Scroll to 0 to reveal menu
                int left = 0;
                scrollView.smoothScrollTo(left, 0);
            } else {
                // Scroll to menuWidth so menu isn't on screen.
                int left = menuWidth;
                scrollView.smoothScrollTo(left, 0);
            }
            menuOut = !menuOut;

        }
    }

}
