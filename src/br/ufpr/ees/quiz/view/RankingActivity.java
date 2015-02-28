package br.ufpr.ees.quiz.view;

import java.util.ArrayList;
import java.util.List;

import br.ufpr.ees.quiz.R;
import br.ufpr.ees.quiz.domain.Pontuacao;
import br.ufpr.ees.quiz.repository.DataBase;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class RankingActivity extends Activity {

	private DataBase db;
	private List<Pontuacao> scores = new ArrayList<Pontuacao>();
	
	private TableLayout tlRanking;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranking);
		
		tlRanking = (TableLayout) findViewById(R.id.tlRanking);
		
		db = new DataBase(this);
		
		loadData();
		
		loadButton();
	}

	private void loadData() {
		
		db.open();
		
		Cursor cursor = db.listScores();
		
		if (cursor.moveToFirst()) {
			do {
				
				Pontuacao score = new Pontuacao(
						cursor.getInt(cursor.getColumnIndex(DataBase.KEY_ID)), 
						cursor.getString(cursor.getColumnIndex(DataBase.KEY_NAME)), 
						cursor.getString(cursor.getColumnIndex(DataBase.KEY_DATE)), 
						cursor.getInt(cursor.getColumnIndex(DataBase.KEY_VALUE)));
				
				scores.add(score);
				
			} while (cursor.moveToNext());
		}
		
		db.close();
		
		if (scores.size() > 0) {
			fillList();
		} else {
			showEmptyScores();
		}
		
	}
	
	private void fillList() {
		
		int count = 0;
		for (Pontuacao pontuacao : scores) {
			
			if (count == 10) {
				break;
			} else {
				count++;
			}
			
			TableRow trRanking = (TableRow) View.inflate(this, R.layout.table_row_ranking, null);
			
			TextView tvName = (TextView) View.inflate(this, R.layout.text_view_ranking, null);
			tvName.setText(pontuacao.getNome());
			trRanking.addView(tvName);

			TextView tvDate = (TextView) View.inflate(this, R.layout.text_view_ranking, null);
			tvDate.setText(pontuacao.getData());
			tvDate.setGravity(Gravity.RIGHT);
			trRanking.addView(tvDate);
			
			TextView tvScore = (TextView) View.inflate(this, R.layout.text_view_ranking, null);
			tvScore.setText(String.valueOf(pontuacao.getValor()));
			tvScore.setGravity(Gravity.RIGHT);
			trRanking.addView(tvScore);
			
			tlRanking.addView(trRanking);
			
		}
		
	}

	private OnClickListener resetListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(RankingActivity.this);
			builder.setTitle(R.string.ranking_reset_message);
			builder.setNegativeButton(R.string.general_cancel, resetDialogListener);
			builder.setPositiveButton(R.string.general_reset, resetDialogListener);

			AlertDialog resultDialog = builder.create();
			resultDialog.show();
			
		}
		
	};
	
	private DialogInterface.OnClickListener resetDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				db.open();
				db.resetScores();
				db.close();
				
				Toast.makeText(RankingActivity.this, R.string.ranking_result_reset_success,
						Toast.LENGTH_LONG).show();
			}

			Intent act = new Intent(RankingActivity.this, MainActivity.class);
			act.putExtra("calling_activity", CallingActivity.RANKING.name());
			startActivity(act);
		}

	};
	
	private OnClickListener restartListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent act = new Intent(RankingActivity.this, GameActivity.class);
			act.putExtra("calling_activity", CallingActivity.RANKING.name());
			startActivity(act);
		}
		
	};
	
	private void loadButton() {
		
		Button btRanking = (Button) findViewById(R.id.btRanking);
		CallingActivity callingActivity = CallingActivity.valueOf(getIntent().getStringExtra("calling_activity"));
		
		if (callingActivity == CallingActivity.MAIN) {
			
			btRanking.setText(R.string.ranking_reset_scores);
			btRanking.setOnClickListener(resetListener);
			
		} else if (callingActivity == CallingActivity.GAME) {
			
			btRanking.setText(R.string.ranking_restart_game);
			btRanking.setOnClickListener(restartListener);
			
		}
		
	}
	
	private void showEmptyScores() {

		TableRow trRanking = (TableRow) View.inflate(this, R.layout.table_row_ranking, null);
		
		TextView tvEmptyMessage = (TextView) View.inflate(this, R.layout.text_view_ranking, null);
		tvEmptyMessage.setText(R.string.ranking_empty_scores);
		tvEmptyMessage.setGravity(Gravity.CENTER);
		trRanking.addView(tvEmptyMessage);
		
		tlRanking.addView(trRanking);
	}
	
	@Override
	public void onBackPressed() {
		Intent act = new Intent(RankingActivity.this, MainActivity.class);
		act.putExtra("calling_activity", CallingActivity.RANKING.name());
		startActivity(act);
	}
	
}
