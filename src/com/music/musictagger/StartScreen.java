package com.music.musictagger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Observable;
import java.util.Observer;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import com.gracenote.mmid.MobileSDK.GNConfig;
import com.gracenote.mmid.MobileSDK.GNOperationStatusChanged;
import com.gracenote.mmid.MobileSDK.GNOperations;
import com.gracenote.mmid.MobileSDK.GNSearchResponse;
import com.gracenote.mmid.MobileSDK.GNSearchResult;
import com.gracenote.mmid.MobileSDK.GNSearchResultReady;
import com.gracenote.mmid.MobileSDK.GNStatus;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StartScreen extends Activity {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private TextView status;
    EditText filepath;
    private GNConfig config;
	private byte[] image;
	private String newtrack,newartist,newalbum,fileparent;
	private String[]filenames=null;
	private int currentfilenumber;
	MusicMetadataSet  dataset=null;
	final int ACTIVITY_CHOOSE_FILE = 1;
	private File currentfile;
	private RecognizeFileOperation op;
	private Alert popup;
	private ProgressDialog progress;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);
        

<<<<<<< HEAD
		config = GNConfig.init("1196288-124407CDD818E9B997FEDEE06B9E47A3",this.getApplicationContext());
=======
		config = GNConfig.init(<API Key>,this.getApplicationContext());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // For each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.title_section1).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.title_section2).setTabListener(this));
>>>>>>> 165272ba25de05a96e7e7f2bd0352038e3b8620e
        
        
        filepath = (EditText)this.findViewById(R.id.filepath);
        
        Button b= (Button) this.findViewById(R.id.Scan);
        b.setText("Scan");
        

        Button browse= (Button) this.findViewById(R.id.Browse);
        browse.setText("browse..");
        
        browse.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent chooseFile;
				Intent intent;
				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFile.setType("file/*");
				intent = Intent.createChooser(chooseFile, "Choose a file");
				startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
			}
        	
        });

        status=(TextView) this.findViewById(R.id.status);
        status.setGravity(Gravity.CENTER);
        
        b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				File sdcard = new File(filepath.getText().toString());
				if (sdcard.exists()) {
					if(sdcard.isDirectory())
					{
						filenames = sdcard.list();
						fileparent = sdcard.getAbsolutePath();
					}
					else
					{
						filenames=new String[]{sdcard.getAbsolutePath()};
						fileparent = sdcard.getParent();
					}
					
					boolean hasFiles = false;
					for (int i=0;i<filenames.length;++i) {
						// Perform simple test for presence of supported music
						// files. Alert user if none are present.
						//Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_LONG).show();
						String lowerCaseFilePath = filenames[i].toLowerCase();
					
						 	if(isValidFile(lowerCaseFilePath)){
								hasFiles = true;
								break;
							}
					}
					if(hasFiles)
					{
						currentfilenumber=0;
						selectFileForFingerprinting();
					}
					else
					{
						updateStatus("Files not Supported.we currently support only mp3 files.",true);
					}
					
				}
				else
					Toast.makeText(getApplicationContext(), "No File/Folder", Toast.LENGTH_LONG).show();
			}
        	
        });
        
    }
    
    protected boolean isValidFile(String lowerCaseFilePath) {
    	if (lowerCaseFilePath.endsWith(".mp3"))
    		return true;
    	return false;
	}

	protected void selectFileForFingerprinting() {
    	if(currentfilenumber<filenames.length)
    	{
    		if(!isValidFile(filenames[currentfilenumber].toLowerCase()))
    		{
    			currentfilenumber++;
    			selectFileForFingerprinting();
    			return;
    		}
    		Toast.makeText(getApplicationContext(), filenames[currentfilenumber], Toast.LENGTH_LONG).show();
    		currentfile=new File(fileparent,filenames[currentfilenumber]);
    		RecognizeFilesTask task=new RecognizeFilesTask();
    		task.execute();
    	}
		
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      switch(requestCode) {
        case ACTIVITY_CHOOSE_FILE: 
        {
          if (resultCode == RESULT_OK)
          {
            Uri uri = data.getData();
            String path = uri.getPath();
            filepath.setText(path);
          }
        }
      }
    }
    
    public class RecognizeFilesTask extends AsyncTask<Object , Object , String> {
    	
    	@Override
        protected void onPreExecute() {
    		progress=new ProgressDialog(StartScreen.this);
    		progress.setMessage("Starting to FingerPrint...");
    		progress.show();
        }

    	@Override
        protected String doInBackground(Object... params) {
    		String resultMsg = null;

			
			if (currentfile.exists()) {
				String file=currentfile.getName();
				String path=currentfile.getAbsolutePath();
				
				op=new RecognizeFileOperation(this, file);
				GNOperations.recognizeMIDFileFromFile(op, config, path);
				
				resultMsg="Success";
			} else {
				resultMsg = "SD Card may be disconnected.Check Properly.";
			}

			return resultMsg;
        }      

        @Override
        protected void onPostExecute(String result) {

        }


    }
    
    class RecognizeFileOperation implements GNSearchResultReady,GNOperationStatusChanged {
    		
    		private RecognizeFilesTask task;
    		private String filePath; // fully qualified name of file getting

		// recognized

    		RecognizeFileOperation(RecognizeFilesTask inTask, String inFilePath) {
    			this.task = inTask;
    			this.filePath = inFilePath;
    		}

			@Override
			public void GNStatusChanged(GNStatus status) {
				updateStatus(status.getMessage(), true);
			}

			@Override
			public void GNResultReady(GNSearchResult result) {
				updateStatus("Got response", true);
				if (result.isFailure()) {
					// An error occurred so display the error to the user.
					String msg = String.format("[%d] %s", result.getErrCode(),result.getErrMessage());
					updateStatus(msg, false); // Display error while leaving the
					// prior status update
					currentfilenumber++;
					selectFileForFingerprinting();
				} else {
					if (result.isFingerprintSearchNoMatchStatus()) {
						// Handle special case of webservices lookup with no match
						updateMetaDataFields((GNSearchResponse) null, true, false);
					} else {
						GNSearchResponse bestResponse = result.getBestResponse();
						updateMetaDataFields(bestResponse, true, false);
						
					}
				}
			}
    }
    
    private void updateMetaDataFields(final GNSearchResponse bestResponse,boolean displayNoCoverArtAvailable, 
    									boolean fromTxtOrLyricSearch)
    {
    	
    	if(bestResponse==null)
    	{
    		AlertDialog.Builder d=new AlertDialog.Builder(StartScreen.this);
    		d.setMessage("Match Not found");
    		d.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					currentfilenumber++;
					selectFileForFingerprinting();
				}
    			
    		});
    		d.show();
    		return;
    	}
    	
    	popup=new Alert(bestResponse);
		this.runOnUiThread(popup);
    	
    
    }
    
    class Alert implements Runnable
    {
    	GNSearchResponse bestResponse;
    	public Alert(GNSearchResponse response)
    	{
    		this.bestResponse=response;
    	}
    	
    	@Override
		public void run() {		
    		newartist=bestResponse.getAlbumArtist();
    		newalbum=bestResponse.getAlbumTitle();
    		newtrack=bestResponse.getTrackTitle();
    		
    		if(bestResponse.getCoverArt()!=null)
    		{
    			new GetCoverArtImage(
    					new Observer() {
    						public void update(Observable observable, Object data) {
    							if(data != null){
    								image = (byte[]) data;
    							}
    						}
    					});
    		}

			String oldartist="",oldtrack="",oldalbum="";
	    	try {
				dataset=new MyID3().read(currentfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(dataset!=null)
			{
				IMusicMetadata data=dataset.getSimplified();
				oldtrack=data.getSongTitle();
				oldalbum=data.getAlbum();
				oldartist=data.getArtist();
			}

			LayoutInflater inflator=StartScreen.this.getLayoutInflater();
	    	View dialoglayout=inflator.inflate(R.layout.dialog_layout, null);
	    	
			ImageView oldartiv=(ImageView)dialoglayout.findViewById(R.id.oldalbumart);
	    	
	    	
	    	oldartiv.setBackgroundResource(R.drawable.no_album_art);
	    	
	    	TextView oldtitletv=(TextView)dialoglayout.findViewById(R.id.oldtitle);
	    	oldtitletv.setText("Old Tags:");
	    	TextView oldtracktv=(TextView)dialoglayout.findViewById(R.id.oldtrack);
	    	oldtracktv.setText(oldtrack);
	    	TextView oldalbumtv=(TextView)dialoglayout.findViewById(R.id.oldalbum);
	    	oldalbumtv.setText(oldalbum);
	    	TextView oldartisttv=(TextView)dialoglayout.findViewById(R.id.oldartist);
	    	oldartisttv.setText(oldartist);
	    	
	    	
	    	AlertDialog.Builder builder=new AlertDialog.Builder(StartScreen.this,AlertDialog.THEME_HOLO_DARK);

	    	builder.setView(dialoglayout);
	    	builder.setTitle("Change the File tags?");
	    	
	    	
	    	ImageView artiv=(ImageView)dialoglayout.findViewById(R.id.newalbumart);
	    	
	    	if(image!=null)
	    		artiv.setImageDrawable(Drawable.createFromStream(new ByteArrayInputStream(image), "src"));
	    	else
	    		artiv.setBackgroundResource(R.drawable.no_album_art);
	    	
	    	TextView titletv=(TextView)dialoglayout.findViewById(R.id.newtitle);
	    	titletv.setText("New Tags:");
	    	TextView tracktv=(TextView)dialoglayout.findViewById(R.id.newtrack);
	    	tracktv.setText(newtrack);
	    	TextView albumtv=(TextView)dialoglayout.findViewById(R.id.newalbum);
	    	albumtv.setText(newalbum);
	    	TextView artisttv=(TextView)dialoglayout.findViewById(R.id.newartist);
	    	artisttv.setText(newartist);
	    	
	    	
	    	
	    	
	    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String parent=null,dstpath=null;
					
					parent=currentfile.getParent();

					dstpath=parent+"/"+newtrack+".mp3";
					File temp=new File(parent,"temp.mp3");
					try {
						temp.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					
					MusicMetadata set=new MusicMetadata("new");
					set.setSongTitle(newtrack);
					set.setAlbum(newalbum);
					set.setArtist(newartist);
					
					try {
						new MyID3().write(currentfile, temp, dataset, set);
						currentfile.delete();
						temp.renameTo(new File(dstpath));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ID3WriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					updateStatus("Tags Changed Succesfully",true);
					currentfilenumber++;
					selectFileForFingerprinting();
					
				}
				
			});
	    	
	    	builder.setNegativeButton("No", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					filepath.setText("");
					filepath.clearFocus();
					currentfilenumber++;
					selectFileForFingerprinting();
					//notify();
				}
			});

	    	builder.show();
		}
    }
    
    class GetCoverArtImage extends AsyncTask<GNSearchResponse, Integer, byte[]> {

		GNSearchResponse bestResponse;
		DelegatedObservable obs = new DelegatedObservable();
		byte[] response;
		public GetCoverArtImage(Observer o) {
			super();
			obs.addObserver(o);
		}

		@Override
		protected  byte[] doInBackground(GNSearchResponse... params) {
			bestResponse = (GNSearchResponse) params[0];
			response = bestResponse.getCoverArt().getData(); 
			return response;
		}

		@Override
		protected void onPostExecute( byte[] result) {
			obs.setChanged();
			obs.notifyObservers(response);
		}

		/**
		 * @author Observable class
		 */
		class DelegatedObservable extends Observable {
			public void clearChanged() {
				super.clearChanged();
			}

			public void setChanged() {
				super.setChanged();
			}
		}
	}
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    public void updateStatus(String result, boolean b) {
    	//Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
		//status.setText(result);
    	if(progress==null || !progress.isShowing())
    		status.setText(result);

    	int p=progress.getProgress();
    	if(result.equals("Got response"))
    	{
    		progress.dismiss();
    	}
    	else if(p!=-1)
    	{
    		progress.setProgress(p+1);
    		progress.setMessage(result);
    	}
	}

	@Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

<<<<<<< HEAD
}
=======
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start_screen, menu);
        return true;
    }

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}
}
>>>>>>> 165272ba25de05a96e7e7f2bd0352038e3b8620e
