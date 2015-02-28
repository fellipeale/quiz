package br.ufpr.ees.quiz.view;

import java.io.IOException;
import java.io.InputStream;

import br.ufpr.ees.quiz.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends Activity {

	//Variables
	
	ListView lvMainMenu;
	ImageView ivQuiz;
	
	//Listeners
	
	OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, final View view,
		          int position, long id) {

			Intent act = new Intent();
			act.putExtra("calling_activity", CallingActivity.MAIN.name());
			
			switch (position) {
			case 0:
				act.setClass(MainActivity.this, GameActivity.class);
				break;
			case 1:
				act.setClass(MainActivity.this, RankingActivity.class);
				break;
			case 2:
				act.setClass(MainActivity.this, AboutActivity.class);
				break;
			}
			
			startActivity(act);
			
		}
    	
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        lvMainMenu = (ListView) findViewById(R.id.lvMainMenu);
        lvMainMenu.setOnItemClickListener(itemClickListener);
        
        ivQuiz = (ImageView) findViewById(R.id.ivQuiz);
        loadImage();
        
    }
    
    private void loadImage() {
    	
    	try {
	    	AssetManager assetManager = getAssets();
	        InputStream istr = assetManager.open("quiz.png");
	        Bitmap bitmap = BitmapFactory.decodeStream(istr);
	        
	        ivQuiz.setImageBitmap(bitmap);
    	} catch (IOException ex) {
    		Log.e("quiz", "Failure to load image", ex);
    	}
    	
	}

	@Override
    public void onBackPressed() {
    	
    	DialogInterface.OnClickListener exitDialogListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					finish();
				}
			}

		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.main_exit_message);
		builder.setNegativeButton(R.string.general_no, exitDialogListener);
		builder.setPositiveButton(R.string.general_yes, exitDialogListener);

		AlertDialog resultDialog = builder.create();
		resultDialog.show();
    	
    }

}
