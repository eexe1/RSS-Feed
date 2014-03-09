package com.example.com.evan.ncu;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class RSSDetailActivity  extends Activity  {
	
	private WebView webView;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_detail);
		 Bundle bundle = this.getIntent().getExtras();
		 
		 String postContent = bundle.getString("content");
		 
		          
		 
		 webView = (WebView)this.findViewById(R.id.RSSwebView);
		 webView.loadData(postContent, "text/html; charset=utf-8","utf-8");

		
	}
}
