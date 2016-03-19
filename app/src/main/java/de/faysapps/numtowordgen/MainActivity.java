package de.faysapps.numtowordgen;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private DBHelper helper;
	private EditText codeET;
	private TextView wordsTV;
	private RadioGroup radioGroup;
	private RadioButton[] radioButtons;
	private ProgressDialog filteringDialog;
	private ProgressDialog loadingDialog;

	private Handler handler;
	private Runnable updateRunnable;
	private Queue<String> wordBuffer;
	/*
	 * wordBuffer stores the words to be written into the textview. used by
	 * GenerateWordsThread and updateUI()
	 */

	private final String DICTIONARY_FILE_NAME = "dictionary_german.txt";

	// private final String DICTIONARY_FILE_NAME = "dict_ger.txt";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		helper = new DBHelper();

		Button button = (Button) findViewById(R.id.button1);
		wordsTV = (TextView) findViewById(R.id.realWordsTV);
		codeET = (EditText) findViewById(R.id.editText1);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		radioButtons = new RadioButton[] {
				(RadioButton) radioGroup.getChildAt(0),
				(RadioButton) radioGroup.getChildAt(1),
				(RadioButton) radioGroup.getChildAt(2) };

		wordsTV.setMovementMethod(new ScrollingMovementMethod());
		button.setOnClickListener(this);

		handler = new Handler();
		wordBuffer = new ConcurrentLinkedQueue<String>();
		updateRunnable = new Runnable() {
			@Override
			public void run() {
				for (@SuppressWarnings("unused")
				String word : wordBuffer) {
					wordsTV.append(wordBuffer.remove() + ", ");
				}
				if (wordsTV.getText().length() != 0) {
					wordsTV.setText(wordsTV.getText().subSequence(0,
							wordsTV.getText().length() - 2));
				} else {
					wordsTV.setText("KEINE WÖRTER GEFUNDEN");
				}
			}
		};
	}

	private class GenerateWordsThread extends Thread {

		/*
		 * starts new MergeWordsThread after each 50'000 words, which invokes
		 * helper.generate...() on the words read until that point of time. The
		 * only goal of this is to save memory. Multithreading is pointless
		 * because reading 1000 words takes longer than processing 1000 words,
		 * except for long words.
		 * 
		 * The detailed procedure is as follows: 1. read 1000 lines and store in
		 * ArrayList 2. copy ArrayList 3. start new MergeWordsThread with the
		 * copy 4. clear the original ArrayList 5. repeat until EOF is reached
		 * 
		 * That way, the (theoretical) max. mem. consumption is 2x ArrayList
		 * with 1000 words each. However, the gc does not quite play by these
		 * rules. It still keeps allocating more mem.
		 */
		private class MergeWordsThread extends Thread {

			ArrayList<String> validWords;
			ArrayList<String> allGermanWords;

			public MergeWordsThread(ArrayList<String> validGermanWords,
					ArrayList<String> allGermanWords) {
				this.validWords = validGermanWords;
				this.allGermanWords = allGermanWords;
			}

			@Override
			public void run() {
				String[] temp = null;
				try {
					temp = helper.generateFilteredWords(allGermanWords);
					for (String tempWord : temp) {
						validWords.add(tempWord);
					}
					allGermanWords.clear();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		Context activity;
		int index;
		ProgressDialog dialog;

		public GenerateWordsThread(Context activity, int index,
				ProgressDialog dialog) {
			this.activity = activity;
			this.index = index;
			this.dialog = dialog;
		}

		@Override
		public void run() {
			long startMillis = System.currentTimeMillis();
			try {
				switch (index) {
				case 2: {
					Set<String> words = helper.generateAllWords();
					for (String word : words) {
						wordBuffer.add(word);
					}
					handler.post(updateRunnable);
					break;
				}
				case 1: {
					String[] words = helper.generateVocalWords();
					for (String word : words) {
						wordBuffer.add(word);
					}
					handler.post(updateRunnable);
					break;
				}
				case 0:
					Log.d("numtowordgen", "case 0");
					InputStream is = getAssets().open(DICTIONARY_FILE_NAME);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is));
					ArrayList<String> allGermanWords = new ArrayList<String>();
					ArrayList<String> validGermanWords = new ArrayList<String>();
					ArrayList<Thread> threads = new ArrayList<Thread>();
					{
						String word; // not used afterwards, hence used as block
										// variable
						int counter = 0;
						/*
						 * TODO: nur den relevanten Bereich der Textdatei
						 * betrachten, d.h. reader.skip() bis zu der Zeile, in
						 * der das erste Wort mit dem gewünschten
						 * Anfangsbuchstaben steht. Und aufhören nach dem
						 * letzten Wort des gewünschten Anfangsbuchstaben, d.h.
						 * für den Code 2345 müssen nur Die Wörter Axxx bis Cxxx
						 * durchsucht werden! Dafür wird Metainfo über die
						 * Textdatei benötigt
						 */
						while ((word = reader.readLine()) != null) {
							allGermanWords.add(word.trim().toLowerCase(
									Locale.GERMANY));
							counter += 1;
							if (counter % 1000 == 0) {
								dialog.incrementProgressBy(1000);
								if (counter % 9000 == 0) {
									Thread t = new MergeWordsThread(
											validGermanWords,
											new ArrayList<String>(
													allGermanWords));
									allGermanWords.clear();
									threads.add(t);
									t.start();
								}
							}
						}
						Log.d("numtowordgen", "" + counter);
						Thread t = new MergeWordsThread(validGermanWords,
								new ArrayList<String>(allGermanWords));
						allGermanWords.clear();
						threads.add(t);
						t.start();
					}
					dialog.dismiss();
					for (Thread t : threads) {
						String aliveString = (t.isAlive()) ? " alive" : " dead";
						t.join();
						Log.d("numtowordgen", "joint " + threads.indexOf(t)
								+ aliveString);
					}
					long endMillis = System.currentTimeMillis();
					Log.d("numtowordgen", "valid words generated.\nDuration: "
							+ (endMillis - startMillis) + "ms\nSize: "
							+ validGermanWords.size());
					for (String word : validGermanWords) {
						wordBuffer.add(word);
					}
					handler.post(updateRunnable);
				}
				dialog.dismiss();
				filteringDialog.dismiss();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d("numtowordgen", e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		String code = codeET.getText().toString();
		if (code.equals("")) {
			Toast.makeText(this, "Bitte geben Sie zuerst einen Code ein",
					Toast.LENGTH_SHORT).show();
			return;
		}
		helper.setCode(codeET.getText().toString());
		wordsTV.setText("");
		int selection = radioGroup.getCheckedRadioButtonId();
		int index = (selection == radioButtons[0].getId()) ? 0
				: (selection == radioButtons[1].getId()) ? 1 : 2;
		filteringDialog = new ProgressDialog(this);
		filteringDialog.setTitle("Berechnen");
		filteringDialog.setMessage("Wörter filtern...");
		filteringDialog.setCancelable(false);
		loadingDialog = new ProgressDialog(this);
		loadingDialog.setTitle("Laden");
		loadingDialog.setMessage("Wörter aus Wörterbuch laden...");
		loadingDialog.setMax(198706);
		loadingDialog.setProgress(0);
		loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		loadingDialog.setCancelable(false);
		ProgressDialog dialog = (index == 0) ? loadingDialog : filteringDialog;
		filteringDialog.show();
		dialog.show();
		new GenerateWordsThread(this, index, dialog).start();
	}

}
