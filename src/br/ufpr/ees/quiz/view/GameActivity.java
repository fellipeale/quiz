package br.ufpr.ees.quiz.view;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufpr.ees.quiz.R;
import br.ufpr.ees.quiz.domain.Questao;
import br.ufpr.ees.quiz.domain.Resposta;
import br.ufpr.ees.quiz.repository.DataBase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity {

	private static final String QUIZWS_URL = "https://quizws-fellipeale.rhcloud.com/questao";

	private List<Questao> questions;
	private Questao actualQuestion;
	private int actualQuestionIndex = 0;
	private int questionsQuantity = 5;
	private int correctAnswers = 0;

	private TextView tvQuestion;
	private List<Button> buttonList;

	private Handler handler;
	private DataBase db;

	private OnClickListener guessButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			submitGuess((Button) v);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		buttonList = new ArrayList<Button>();
		handler = new Handler();
		db = new DataBase(this);

		tvQuestion = (TextView) findViewById(R.id.tvQuestion);
		Button btAnswer1 = (Button) findViewById(R.id.btAnswer1);
		btAnswer1.setOnClickListener(guessButtonListener);
		buttonList.add(btAnswer1);

		Button btAnswer2 = (Button) findViewById(R.id.btAnswer2);
		btAnswer2.setOnClickListener(guessButtonListener);
		buttonList.add(btAnswer2);

		Button btAnswer3 = (Button) findViewById(R.id.btAnswer3);
		btAnswer3.setOnClickListener(guessButtonListener);
		buttonList.add(btAnswer3);

		Button btAnswer4 = (Button) findViewById(R.id.btAnswer4);
		btAnswer4.setOnClickListener(guessButtonListener);
		buttonList.add(btAnswer4);

		try {
			loadQuestions();
			
			resetGame();
		} catch (Exception ex) {
			
			Toast.makeText(this, R.string.game_ws_error_message,
					Toast.LENGTH_LONG).show();
			
			Intent act = new Intent(GameActivity.this, MainActivity.class);
			act.putExtra("calling_activity", CallingActivity.RANKING.name());
			startActivity(act);
			
		}

	}

	private void resetGame() {

		if (actualQuestion == null || !actualQuestion.equals(questions.get(0))) {
			actualQuestion = questions.get(0);
		}

		fillFields();

	}

	private void nextQuestion() {

		actualQuestionIndex++;
		actualQuestion = questions.get(actualQuestionIndex);

		fillFields();

	}

	private void fillFields() {

		tvQuestion.setText(actualQuestion.getValor());
		tvQuestion.setTag(actualQuestion);

		buttonList.get(0).setText(
				actualQuestion.getRespostas().get(0).getValor());
		buttonList.get(0).setTag(actualQuestion.getRespostas().get(0));
		buttonList.get(0).setBackgroundColor(Color.LTGRAY);
		buttonList.get(1).setText(
				actualQuestion.getRespostas().get(1).getValor());
		buttonList.get(1).setTag(actualQuestion.getRespostas().get(1));
		buttonList.get(1).setBackgroundColor(Color.LTGRAY);
		buttonList.get(2).setText(
				actualQuestion.getRespostas().get(2).getValor());
		buttonList.get(2).setTag(actualQuestion.getRespostas().get(2));
		buttonList.get(2).setBackgroundColor(Color.LTGRAY);
		buttonList.get(3).setText(
				actualQuestion.getRespostas().get(3).getValor());
		buttonList.get(3).setTag(actualQuestion.getRespostas().get(3));
		buttonList.get(3).setBackgroundColor(Color.LTGRAY);

	}

	private void submitGuess(Button guessButton) {

		showCorrectAnswer(guessButton);

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {

				if ((actualQuestionIndex + 1) == questionsQuantity) {
					showResultsDialog();
				} else {
					nextQuestion();
				}
			}

		}, 500);

	}

	private void showResultsDialog() {

		final LinearLayout llDialog = new LinearLayout(this);
		llDialog.setOrientation(LinearLayout.VERTICAL);

		final TextView tvMessage = new TextView(this);
		tvMessage.setText(MessageFormat.format(
				getResources().getString(R.string.game_result_message),
				correctAnswers * 20)
				+ "\n"
				+ getResources().getString(R.string.game_result_save_message));
		tvMessage.setGravity(Gravity.CENTER);
		tvMessage.setTextColor(Color.WHITE);
		llDialog.addView(tvMessage);

		final EditText etName = new EditText(this);
		llDialog.addView(etName);

		DialogInterface.OnClickListener resultsDialogListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					saveResult(etName.getText().toString());
				}

				Intent act = new Intent(GameActivity.this,
						RankingActivity.class);
				act.putExtra("calling_activity", CallingActivity.GAME.name());
				startActivity(act);
			}

		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.game_finished_label);
		builder.setView(llDialog);
		builder.setNegativeButton(R.string.general_cancel,
				resultsDialogListener);
		builder.setPositiveButton(R.string.general_save, resultsDialogListener);

		AlertDialog resultDialog = builder.create();
		resultDialog.show();

	}

	private void showCorrectAnswer(Button guessButton) {

		Resposta guess = (Resposta) guessButton.getTag();

		if (guess.isCorreta()) {
			correctAnswers++;
			guessButton.setBackgroundColor(Color.GREEN);
		} else {
			guessButton.setBackgroundColor(Color.RED);

			for (Button button : buttonList) {
				Resposta answer = (Resposta) button.getTag();
				if (answer.isCorreta()) {
					button.setBackgroundColor(Color.GREEN);
				}
			}
		}

	}

	private void saveResult(String name) {

		db.open();

		db.insertScore(name, correctAnswers * 20);

		db.close();

		Toast.makeText(this, R.string.game_result_saved_success,
				Toast.LENGTH_LONG).show();
	}

	private void loadQuestions() {

		Thread thread = new Thread() {
			@Override
			public void run() {

				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet get = new HttpGet(QUIZWS_URL + "/quantidade/"
							+ questionsQuantity);

					HttpResponse response = client.execute(get);
					HttpEntity entity = response.getEntity();

					if (entity != null) {

						InputStream stream = entity.getContent();

						String questionsJSON = IOUtils.toString(stream);
						stream.close();

						questions = getQuestionsFromJSON(questionsJSON);

					}
				} catch (Exception ex) {
					Log.e("quiz", "Failure acessing WebService", ex);
				}

			};
		};

		thread.start();

		try {
			thread.join();
		} catch (InterruptedException ex) {
			Log.e("quiz", "Error running thread", ex);
		}

	}

	private List<Questao> getQuestionsFromJSON(String questionsJSON) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			List<Questao> parsedQuestoes = new ArrayList<Questao>();
			parsedQuestoes = mapper.readValue(questionsJSON,
					new TypeReference<List<Questao>>() {
					});

			return parsedQuestoes;
		} catch (Exception ex) {
			Log.e("quiz", "Failure to convert JSON", ex);
		}

		return null;

	}

	@Override
	public void onBackPressed() {

		DialogInterface.OnClickListener exitDialogListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					Intent act = new Intent(GameActivity.this, MainActivity.class);
					act.putExtra("calling_activity", CallingActivity.RANKING.name());
					startActivity(act);
				}
			}

		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.game_exit_message);
		builder.setNegativeButton(R.string.general_no, exitDialogListener);
		builder.setPositiveButton(R.string.general_yes, exitDialogListener);

		AlertDialog resultDialog = builder.create();
		resultDialog.show();

	}
}
