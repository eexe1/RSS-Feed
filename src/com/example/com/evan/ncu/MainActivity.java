package com.example.com.evan.ncu;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	private ArrayList<PostData> listData = new ArrayList<PostData>();
	private PullToRefreshListView listView;
	private PostItemAdapter itemAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// this.generateDummyData();
		new HTTPDownloadTask().execute("YOUR RSS URL HERE");

		listView = (PullToRefreshListView) this.findViewById(R.id.postListView);
		itemAdapter = new PostItemAdapter(this, R.layout.postitem, listData);
		listView.setAdapter(itemAdapter);
		listView.setOnRefreshListener(new OnRefreshListener() {

			public void onRefresh() {
				// Your code to refresh the list contents goes here
				// for example:
				// If this is a webservice call, it might be asynchronous so
				// you would have to call listView.onRefreshComplete(); when
				// the webservice returns the data
				// Make sure you call listView.onRefreshComplete()
				// when the loading is done. This can be done from here or any
				// other place, like on a broadcast receive from your loading
				// service or the onPostExecute of your AsyncTask.

				// For the sake of this sample, the code will pause here to
				// force a delay when invoking the refresh
				itemAdapter.refresh(listData);
				listView.postDelayed(new Runnable() {

					public void run() {
						listView.onRefreshComplete();
					}
				}, 2000);
			}
		});
		listView.setOnItemClickListener(onItemClickListener);

	}
	private class HTTPDownloadTask extends
			AsyncTask<String, Integer, ArrayList<PostData>> {
		private RSSXMLTag currentTag;
		private ArrayList<PostData> postDataList = new ArrayList<PostData>();

		@Override
		protected ArrayList<PostData> doInBackground(String... params) {

			// TODO Auto-generated method stub
			String urlStr = params[0];
			InputStream is = null;

			try {

				URL url = new URL(urlStr);

				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setReadTimeout(10 * 1000);
				connection.setConnectTimeout(10 * 1000);
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				connection.connect();

				int response = connection.getResponseCode();
				Log.d("Debug", "The response is: " + response);
				is = url.openConnection().getInputStream();
				// parse xml after getting the data
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser xpp = factory.newPullParser();
				xpp.setInput(is, null);

				int eventType = xpp.getEventType();
				PostData pdData = null;
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"EEE, DD MMM yyyy ");
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_DOCUMENT) {

					} else if (eventType == XmlPullParser.START_TAG) {
						if (xpp.getName().equals("item")) {
							pdData = new PostData();
							currentTag = RSSXMLTag.IGNORETAG;
						} else if (xpp.getName().equals("title")) {
							currentTag = RSSXMLTag.TITLE;
						} else if (xpp.getName().equals("link")) {
							currentTag = RSSXMLTag.LINK;
						} else if (xpp.getName().equals("pubDate")) {
							currentTag = RSSXMLTag.DATE;
						} else if (xpp.getName().equals("description")) {
							currentTag = RSSXMLTag.DESCRIPTION;
						}
					} else if (eventType == XmlPullParser.END_TAG) {
						if (xpp.getName().equals("item")) {
							// format the data here, otherwise format data in
							// Adapter
							Date postDate = dateFormat.parse(pdData.postDate);
							pdData.postDate = dateFormat.format(postDate);
							postDataList.add(pdData);
						} else {
							currentTag = RSSXMLTag.IGNORETAG;
						}
					} else if (eventType == XmlPullParser.TEXT) {
						String content = xpp.getText();
						content = content.trim();
						// /Log.d("debug", content);
						if (pdData != null) {
							switch (currentTag) {
							case TITLE:
								if (content.length() != 0) {
									if (pdData.postTitle != null) {
										pdData.postTitle += content;
									} else {
										pdData.postTitle = content;
									}
								}
								break;
							case LINK:
								if (content.length() != 0) {
									if (pdData.postLink != null) {
										pdData.postLink += content;
									} else {
										pdData.postLink = content;
									}
								}
								break;
							case DATE:
								if (content.length() != 0) {
									if (pdData.postDate != null) {
										pdData.postDate += content;
									} else {
										pdData.postDate = content;
									}
								}
								break;
							case DESCRIPTION:
								
								if (content.length() != 0) {
									
									//No need to be parsed, webView will automatically parse it.
									/*
									//Log.e("Debug","NEXT");
									//Log.e("Debug",content);	
									Pattern pattern = Pattern.compile("\n");
									Matcher matcher = pattern.matcher(content);
									//Log.e("Debug",matcher.replaceAll(""));
									content = matcher.replaceAll("");
									
									pattern = Pattern.compile("&nbsp;");
									matcher = pattern.matcher(content);
									//Log.e("Debug",matcher.replaceAll(""));
									content = matcher.replaceAll("");

									pattern = Pattern.compile(" ");
									matcher = pattern.matcher(content);
									//Log.e("Debug",matcher.replaceAll(""));
									content = matcher.replaceAll("");
									
									
									String temp = "";
									
									pattern = Pattern.compile("<.*?>");
									
									String[] words = pattern.split(content);
									
									for (String s : words) {	
										if(!s.equals(" ")){
											temp += s;
											//Log.e("Debug",s);		
										}
									}
											//Log.e("Debug",temp);	
									content = temp;
									*/
									if (pdData.postDetail != null) {
										pdData.postDetail += content;
									} else {
										pdData.postDetail = content;
									}
								}
								break;
							default:
								break;
							}
						}
					}

					eventType = xpp.next();
				}
				// Log.v("Debug", String.valueOf((postDataList.size())));

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return postDataList;
		}

		@Override
		protected void onPostExecute(ArrayList<PostData> result) {
			// TODO Auto-generated method stub
			Log.e("Debug", "onPostExecute");
			for (int i = 0; i < result.size(); i++) {
				listData.add(result.get(i));
				// Log.e("Debug",listData.get(i).postDetail);
			}
			itemAdapter.refresh(listData);
			listView.postDelayed(new Runnable() {
				public void run() {
					listView.onRefreshComplete();
				}
			}, 2000);
		}
	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			PostData data = listData.get(arg2);
			
			Bundle postInfo = new Bundle();
			postInfo.putString("content", data.postDetail);
			
			Intent postviewIntent = new Intent(MainActivity.this, RSSDetailActivity.class);
			postviewIntent.putExtras(postInfo);
			startActivity(postviewIntent);
		}
	};

}
